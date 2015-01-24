package com.albertcbraun.cms50fw.alert;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.albertcbraun.cms50fwlib.BluetoothNotAvailableException;
import com.albertcbraun.cms50fwlib.BluetoothNotEnabledException;
import com.albertcbraun.cms50fwlib.CMS50FWBluetoothConnectionManager;
import com.albertcbraun.cms50fwlib.CMS50FWConnectionListener;
import com.albertcbraun.cms50fwlib.DataFrame;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Holds the UI elements for the main CMS50FW alert functionality.
 */
public class MainUIFragment extends Fragment {

    public static final String TAG = MainUIFragment.class.getSimpleName();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.US);
    private static final String MAXIMUM_SPO2_PERCENTAGE_MAX_VALUE_KEY_NAME = "MAXIMUM_SPO2_PERCENTAGE_MAX_VALUE_KEY_NAME";
    private static final String CURRENT_SPO2_PERCENTAGE_MAX_VALUE_KEY_NAME = "CURRENT_SPO2_PERCENTAGE_MAX_VALUE_KEY_NAME";
    private static final String BPM_STRING = " bpm";
    private static final String PERCENT_SIGN_STRING = "%";
    private static final String EMPTY_STRING = "";
    private static final String FINGER_OUT_MESSAGE = "Finger Out";
    private static final String FINGER_OUT_TOO_LONG_MESSAGE = "Finger Out For Too Many Seconds";
    private static final String OXYGEN_LEVEL_TOO_LOW_MESSAGE = "Oxygen Level Too Low For Too Many Seconds";
    private static final String CMS50FW_BLUETOOTH_DEVICE_NAME = "SpO202";
    private static final String DATA_FRAME_NULL_ALARM_MESSAGE = "Bluetooth connection to CMS50FW has apparently been lost";
    private static final String PLEASE_TURN_BLUETOOTH_ON_MESSAGE = "Please turn Bluetooth on in the Settings for this Android device.";
    private static final String BLUETOOTH_FUNCTIONALITY_IS_NOT_SUPPORTED_MESSAGE = "Bluetooth functionality is not supported on this Android device.";
    private static final String NEWLINE = "\n";
    private static final String LEFT_BRACKET = " [";
    private static final String RIGHT_BRACKET = "] ";
    //private static final String FINGER_IN_LOG_MESSAGE_PREFIX = "FINGER_IN:";
    //private static final String FINGER_OUT_LOG_MESSAGE_PREFIX = "FINGER_OUT:";

    private static final int MAX_MESSAGE_WINDOW_CHARS = 5000;
    private static final int LOWER_INDEX_MESSAGE_WINDOW_CHARS = 1000;
    private static final int MAXIMUM_SPO2_PERCENTAGE_DEFAULT_VALUE = 99;
    private static final int CURRENT_SPO2_PERCENTAGE_DEFAULT_VALUE = 80;
    private static final int FINGER_OUT_MESSAGE_THRESHOLD = 10;
    private static final int FINGER_OUT_ALARM_THRESHOLD = 600;
    private static final int OXYGEN_LEVEL_TOO_LOW_ALARM_THRESHOLD = 600;
    private static final int DATA_FRAME_NULL_ALARM_THRESHOLD = 600;
    private static final int SRC_QUALITY = 0;
    private static final int MAX_STREAMS = 1;
    //private static final String UNEXPECTED_DATA_FRAME_VALUES = "Unexpected Data Frame values:";
    private static final String SEARCHING_FOR_SIGNAL_MESSAGE = "Searching for O2 level and pulse ...";
    private static final int ONE_HUNDRED = 100;

    // alarm and sound related properties
    Integer minimumSpo2Percentage = null;
    private long consecutiveFingerOutDataFrameCount = 0;
    private long consecutiveSpO2OutOfRangeCount = 0;
    boolean uiAlertSet;
    boolean alertSoundEnabled = true;
    SoundPool soundPool = null;
    int soundStreamId = -1;

    // facilitate connections with the threads that talk directly to the CMS50FW
    private CMS50FWBluetoothConnectionManager cms50FWBluetoothConnectionManager = null;

    // UI elements and properties
    private Button connectButton = null;
    private TextView messageWindow = null;
    private TextView timeWindow = null;
    private TextView spo2Window = null;
    private TextView pulseWindow = null;
    private ScrollView messageWindowScrollView = null;
    private int consecutiveDataFrameNullCount = 0;

    public MainUIFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");

        // keep this fragment around even if app and resource reconfiguration occurs
        // so this instance can hold on to the live bluetooth connection and IO streams.
        // this is desirable because we want the alarm functionality to be durable across
        // app lifecycle and teardown/restart events, to the degree possible
        setRetainInstance(true);

        // set up apparatus for bluetooth communication with the CMS50
        cms50FWBluetoothConnectionManager = new CMS50FWBluetoothConnectionManager(CMS50FW_BLUETOOTH_DEVICE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_main_activity, container, false);

        // UI elements
        messageWindow = (TextView) rootView.findViewById(R.id.message_window);
        messageWindowScrollView = (ScrollView) rootView.findViewById(R.id.message_window_scroll_view);
        timeWindow = (TextView) rootView.findViewById(R.id.current_time_window);
        spo2Window = (TextView) rootView.findViewById(R.id.current_spo2_window);
        pulseWindow = (TextView) rootView.findViewById(R.id.current_pulse_window);
        connectButton = (Button) rootView.findViewById(R.id.connect_button);
        Button startReadingDataButton = (Button) rootView.findViewById(R.id.start_reading_data_button);
        Button stopReadingDataButton = (Button) rootView.findViewById(R.id.stop_reading_data_button);
        TextView minimumSpo2PercentageText = (TextView) rootView.findViewById(R.id.minimum_spo2_percentage_text);
        minimumSpo2PercentageText.addTextChangedListener(new Spo2PercentageTextChangedListener(this));
        SeekBar minimumSpo2PercentageSeekBar = (SeekBar) rootView.findViewById(R.id.minimum_spo2_percentage);
        setUpSeekBar(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()),
                minimumSpo2PercentageSeekBar, minimumSpo2PercentageText, minimumSpo2Percentage,
                MAXIMUM_SPO2_PERCENTAGE_MAX_VALUE_KEY_NAME, MAXIMUM_SPO2_PERCENTAGE_DEFAULT_VALUE,
                CURRENT_SPO2_PERCENTAGE_MAX_VALUE_KEY_NAME, CURRENT_SPO2_PERCENTAGE_DEFAULT_VALUE);

        connectButton.setEnabled(true);
        startReadingDataButton.setEnabled(false);
        stopReadingDataButton.setEnabled(false);

        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_ALARM, SRC_QUALITY);

        // set a custom callback which is fully aware of the main fragment's UI
        CMS50FWConnectionListener cms50fwCallbacks = new CMS50FWCallbacks(this, connectButton,
                startReadingDataButton, stopReadingDataButton);
        this.cms50FWBluetoothConnectionManager.setCMS50FWConnectionListener(cms50fwCallbacks);

        return rootView;
    }

    private void setUpSeekBar(SharedPreferences defaultSettingsPreference, SeekBar seekBar,
                              TextView correspondingTextView, Integer correspondingInteger,
                              @SuppressWarnings("SameParameterValue") String maxValueSharedPrefsKeyName, int defaultMaxValue,
                              String currentValueSharedPrefsKeyName, int defaultCurrValue) {

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBarChangeListener(getActivity(),
                correspondingTextView, correspondingInteger, currentValueSharedPrefsKeyName);

        seekBar.setMax(defaultSettingsPreference.getInt(maxValueSharedPrefsKeyName, defaultMaxValue));
        int currValue = defaultSettingsPreference.getInt(currentValueSharedPrefsKeyName, defaultCurrValue);
        correspondingTextView.setText(String.valueOf(currValue));
        seekBar.setProgress(currValue);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    public void connect(View view) {
        try {
            this.cms50FWBluetoothConnectionManager.connect(view.getContext());
            connectButton.setEnabled(false);
        } catch(BluetoothNotAvailableException e) {
            writeMessage(System.currentTimeMillis(), BLUETOOTH_FUNCTIONALITY_IS_NOT_SUPPORTED_MESSAGE);
            Log.w(TAG, e.toString());
        } catch (BluetoothNotEnabledException e) {
            writeMessage(System.currentTimeMillis(), PLEASE_TURN_BLUETOOTH_ON_MESSAGE);
            Log.w(TAG, e.toString());
        }
    }

    public void resetState(@SuppressWarnings("UnusedParameters") View v) {
        unsetUIAlert();
        cms50FWBluetoothConnectionManager.reset();
    }

    public void startData(@SuppressWarnings("UnusedParameters") View view) {
        cms50FWBluetoothConnectionManager.startData();
    }

    public void stopData(@SuppressWarnings("UnusedParameters") View ignored) {
        cms50FWBluetoothConnectionManager.stopData();
    }

    public void clearWindow(@SuppressWarnings("UnusedParameters") View v) {
        messageWindow.post(new Runnable() {
            @Override
            public void run() {
                messageWindow.setText(EMPTY_STRING);
            }
        });
    }

    void updateUI(long time, final String spo2, final String pulse) {
        final Date d = new Date(time);
        timeWindow.setText(DATE_FORMAT.format(d));
        spo2Window.setText(spo2);
        pulseWindow.setText(pulse);
    }

    void disableAlertSound() {
        alertSoundEnabled = false;
    }

    void enableAlertSound() {
        alertSoundEnabled = true;
    }

    void setUIAlert(final String alertMessage) {
        this.onResume();
        if (!uiAlertSet) {
            if (getView() != null) {
                getView().post(new ActivateAlertTask(this, alertMessage));
            }
        }
    }

    void unsetUIAlert() {
        if (uiAlertSet) {
            if (getView() != null) {
                getView().post(new DeactivateAlertTask(this));
            }
        }
    }

    void stopAlertSound() {
        if (soundPool != null && soundStreamId > -1) {
            soundPool.stop(soundStreamId);
        }
    }

    void writeMessage(long timeStamp, final String s) {
        final Date d = new Date(timeStamp);
        messageWindow.post(new Runnable() {
            @Override
            public void run() {
                String text = messageWindow.getText().toString();
                if (text.length() > MAX_MESSAGE_WINDOW_CHARS) {
                    messageWindow.setText(text.substring(LOWER_INDEX_MESSAGE_WINDOW_CHARS, text.length() - 1));
                }
                messageWindow.append(NEWLINE + LEFT_BRACKET + DATE_FORMAT.format(d) + RIGHT_BRACKET + s);
                messageWindowScrollView.post(
                        new Runnable() {
                            public void run() {
                                messageWindowScrollView.fullScroll(View.FOCUS_DOWN);
                            }
                        });

            }
        });
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy!!");
        super.onDestroy();
        cms50FWBluetoothConnectionManager.dispose(getActivity().getApplicationContext());
    }

    public void processDataFrame(DataFrame dataFrame) {
        if (dataFrame == null) {
            consecutiveDataFrameNullCount++;
            if (consecutiveDataFrameNullCount > DATA_FRAME_NULL_ALARM_THRESHOLD) {
                setUIAlert(DATA_FRAME_NULL_ALARM_MESSAGE);
            }
        } else {
            consecutiveDataFrameNullCount = 0;
            if (dataFrame.spo2Percentage <= ONE_HUNDRED) { // valid data frame
                consecutiveFingerOutDataFrameCount = 0;
                //Log.v(TAG, FINGER_IN_LOG_MESSAGE_PREFIX + dataFrame.toString());
                updateUI(dataFrame.time, dataFrame.spo2Percentage + PERCENT_SIGN_STRING, dataFrame.pulseRate + BPM_STRING);
                if (dataFrame.spo2Percentage < minimumSpo2Percentage) {
                    consecutiveSpO2OutOfRangeCount++;
                } else {
                    consecutiveSpO2OutOfRangeCount = 0;
                }
                if (consecutiveSpO2OutOfRangeCount > OXYGEN_LEVEL_TOO_LOW_ALARM_THRESHOLD) {
                    setUIAlert(OXYGEN_LEVEL_TOO_LOW_MESSAGE);
                } else {
                    unsetUIAlert();
                }
            } else { // probably not valid data frame
                if (dataFrame.isFingerOutOfSleeve) {
                    consecutiveFingerOutDataFrameCount++;
                    if (consecutiveFingerOutDataFrameCount > FINGER_OUT_MESSAGE_THRESHOLD) {
                        //Log.v(TAG, FINGER_OUT_LOG_MESSAGE_PREFIX + dataFrame.toString());
                        updateUI(dataFrame.time, FINGER_OUT_MESSAGE, EMPTY_STRING);
                    }
                    if (consecutiveFingerOutDataFrameCount > FINGER_OUT_ALARM_THRESHOLD) {
                        setUIAlert(FINGER_OUT_TOO_LONG_MESSAGE);
                    }
                } else {
                    //Log.w(TAG, UNEXPECTED_DATA_FRAME_VALUES + dataFrame.toString());
                    updateUI(dataFrame.time, SEARCHING_FOR_SIGNAL_MESSAGE, EMPTY_STRING);
                }
            }
        }
    }

}
