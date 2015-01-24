package com.albertcbraun.cms50fwlib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Verifies that underlying BluetoothAdapter exists and is ready to go, finds the
 * desired Bluetooth device, connects to it, and opens input and output streams.
 * <p>
 * Also tries to ensure that the connection stays alive during use, but provides
 * methods to shut it down (reset everything) when desired.
 * <p>
 * Created by albertb on 1/10/2015.
 */
class AndroidBluetoothConnectionComponents {

    private static final String TAG = AndroidBluetoothConnectionComponents.class.getSimpleName();
    private static final String BLUETOOTH_IS_NOT_SUPPORTED_ON_THIS_ANDROID_DEVICE_MESSAGE = "Bluetooth is not supported on this android device!!";
    private static final String BLUETOOTH_IS_NOT_ENABLED_MESSAGE = "Bluetooth is not enabled. Please go to Settings and enable Bluetooth on this Android device.";
    private static final String SETTING_UP_BROADCAST_RECEIVER_MESSAGE = "Setting up BroadcastReceiver";
    private static final String DONE_REGISTERING_BROADCAST_RECEIVER_MESSAGE = "Done registering BroadcastReceiver.";
    private static final String CANCELING_PREVIOUS_BLUETOOTH_DISCOVERY_MESSAGE = "Canceling previous Bluetooth discovery.";
    private static final String INITIATING_BLUETOOTH_DISCOVERY_OF_CMS50_FW_DEVICE_MESSAGE = "Initiating bluetooth discovery of CMS50FW device.";
    private static final String BLUETOOTH_IS_NOT_TURNED_ON_MESSAGE = "Could not start bluetooth discovery. Bluetooth is not in STATE_ON on this device";
    private static final String JUST_STARTED_BLUETOOTH_DISCOVERY_MESSAGE = "Just started Bluetooth discovery";
    private static final String COULD_NOT_WRITE_COMMAND_MESSAGE = "Could not write command %d to output stream. Bluetooth socket is not connected.";
    private static final String STARTING_RESET_MESSAGE = "Starting reset";
    private static final String CLOSING_BLUETOOTH_SOCKET_AND_IO_STREAMS_MESSAGE = "Closing Bluetooth socket and I/O streams.";
    private static final String OUTPUT_STREAM = "OutputStream";
    private static final String INPUT_STREAM = "InputStream";
    private static final String BLUETOOTH_SOCKET = "Bluetooth Socket";
    private static final String RESET_COMPLETE_MESSAGE = "Reset complete";
    private static final String CLOSED_FORMAT_STRING = "Closed %s";
    private static final String COULD_NOT_CLOSE_FORMAT_STRING = "Could not close %s";
    private static final UUID DEFAULT_BLUETOOTH_SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String COULD_NOT_UNREGISTER_BROADCAST_RECEIVER_PROCEEDING_ANYWAY_MESSAGE = "Could not unregister BroadcastReceiver because it was apparently never registered. Proceeding anyway.";
    private UUID bluetoothServiceUUID = DEFAULT_BLUETOOTH_SERVICE_UUID;
    private static final int COMMAND_ONE_TWENTY_NINE = 129;
    private final String androidBluetoothDeviceName;
    volatile InputStream inputStream = null;
    volatile boolean okToReadData;
    private boolean broadcastReceiverIsRegistered = false;
    private CMS50FWBluetoothConnectionManager cms50FWBluetoothConnectionManager = null;
    private CMS50FWConnectionListener cms50FWConnectionListener = null;
    private volatile BluetoothDevice cms50FWDevice = null;
    private volatile BluetoothSocket bluetoothSocket = null;
    private volatile OutputStream outputStream = null;
    private BroadcastReceiver broadcastReceiver = null;
    private BluetoothAdapter bluetoothAdapter = null;


    /**
     * Registers a default, logging-only, CMS50FWConnectionListener which you
     * should replace later, using {@link #setCms50FWConnectionListener(CMS50FWConnectionListener)}.
     *
     * @param cms50FWBluetoothConnectionManager the front end of this framework
     * @param cms50FWConnectionListener callbacks for the client app
     * @param androidBluetoothDeviceName the string which represents the name of the bluetooth device we're looking for (e.g. "SpO202")
     */
    AndroidBluetoothConnectionComponents(CMS50FWBluetoothConnectionManager cms50FWBluetoothConnectionManager,
                                                CMS50FWConnectionListener cms50FWConnectionListener,
                                                String androidBluetoothDeviceName) {
        this.cms50FWBluetoothConnectionManager = cms50FWBluetoothConnectionManager;
        this.androidBluetoothDeviceName = androidBluetoothDeviceName;
        this.cms50FWConnectionListener = cms50FWConnectionListener;
    }

    /**
     * Cleans up bluetooth connections, sockets, streams, etc.
     */
    void dispose(Context context) {
        unregisterBroadcastReceiver(context);
        reset();
    }

    /**
     * Set the custom instance of {@link com.albertcbraun.cms50fwlib.CMS50FWConnectionListener} for your
     * app here. This is really useful because it informs your app about the state
     * of the bluetooth adapter, connection, progress reading data, etc.
     *
     * @param cms50FWConnectionListener callbacks for the client app
     */
    void setCms50FWConnectionListener(CMS50FWConnectionListener cms50FWConnectionListener) {
        this.cms50FWConnectionListener = new ConnectionListenerForwarder(cms50FWConnectionListener);
    }

    /**
     * A convenient way to provide the callback object to other classes that need it.
     *
     * @return a listener object for communicating important data back to the client app
     */
    CMS50FWConnectionListener getCMS50FWConnectionListener() {
        return cms50FWConnectionListener;
    }

    /**
     * A convenient way to provide the connection manager to other classes that need it.
     *
     * @return the frontend object of this library used by the client app
     */
    CMS50FWBluetoothConnectionManager getCMS50FWBluetoothConnectionManager() {
        return cms50FWBluetoothConnectionManager;
    }

    /**
     * Calls back to the callback listener's onLogEvent method, supplying a timestamp
     * in the process.
     *
     * @param message a message to be sent to the client app or made visible to the user somehow
     */
    private void logEvent(String message) {
        cms50FWConnectionListener.onLogEvent(System.currentTimeMillis(), message);
    }

    /**
     * Verifies that Bluetooth connections are possible from the current Android device.
     * Then, registers a temporary, custom BroadcastReceiver and starts bluetooth discovery.
     * The rest of the work of obtaining the Bluetooth device, connecting, obtaining a
     * Bluetooth socket, and obtaining IO streams will be done after the device
     * is actually discovered, in the CMS50FWBroadcastReceiver.
     */
    void findAndConnect(Context context) throws  BluetoothNotAvailableException, BluetoothNotEnabledException {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.w(TAG, BLUETOOTH_IS_NOT_SUPPORTED_ON_THIS_ANDROID_DEVICE_MESSAGE);
            logEvent(BLUETOOTH_IS_NOT_SUPPORTED_ON_THIS_ANDROID_DEVICE_MESSAGE);
            throw new BluetoothNotAvailableException();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Log.w(TAG, BLUETOOTH_IS_NOT_ENABLED_MESSAGE);
                logEvent(BLUETOOTH_IS_NOT_ENABLED_MESSAGE);
                throw new BluetoothNotEnabledException();
            }
        }

        // cancel any existing discovery which may be ongoing
        if (bluetoothAdapter.isDiscovering()) {
            logEvent(CANCELING_PREVIOUS_BLUETOOTH_DISCOVERY_MESSAGE);
            bluetoothAdapter.cancelDiscovery();
        }

        // remove previous broadcast receiver if it is still registered
        unregisterBroadcastReceiver(context);

        // set up broadcast receiver - will be torn down after making connection
        this.logEvent(SETTING_UP_BROADCAST_RECEIVER_MESSAGE);
        broadcastReceiver = new CMS50FWBroadcastReceiver();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        context.registerReceiver(broadcastReceiver, filter);
        broadcastReceiverIsRegistered = true;
        logEvent(DONE_REGISTERING_BROADCAST_RECEIVER_MESSAGE);

        // initiate Bluetooth discovery, which will invoke the BroadcastReceiver later
        logEvent(INITIATING_BLUETOOTH_DISCOVERY_OF_CMS50_FW_DEVICE_MESSAGE);
        if (!bluetoothAdapter.startDiscovery()) {
            logEvent(BLUETOOTH_IS_NOT_TURNED_ON_MESSAGE);
        }
        Log.v(TAG, JUST_STARTED_BLUETOOTH_DISCOVERY_MESSAGE);
    }

    /**
     * Verifies that the various components (socket, streams, etc) needed
     * for a useful connection to the Bluetooth device are still viable.
     *
     * @return true if the plumbing to the Bluetooth device appears to still be working.
     */
    boolean connectionAlive() {
        return bluetoothAdapter.isEnabled() && cms50FWDevice != null &&
                inputStream != null && outputStream != null &&
                bluetoothSocket != null && bluetoothSocket.isConnected();
    }

    /**
     *
     * @param command a single command from the custom enum {@link com.albertcbraun.cms50fwlib.CMS50FWCommand}
     * @param dataByte an additional byte. if the command does not require any additional data,
     *                 this should be {@link com.albertcbraun.cms50fwlib.CMS50FWCommand#PADDING}.
     * @throws IOException if the write attempt fails and the command is not written back to the device
     *          (because, for example, of a broken Bluetooth connection caused by a Bluetooth device
     *          which has been shut down or moved out of Bluetooth range).
     *
     */
    @SuppressWarnings("SameParameterValue")
    void writeCommand(CMS50FWCommand command, CMS50FWCommand dataByte) throws IOException {
        if (connectionAlive()) {
            outputStream.write(CMS50FWCommand.COMMAND_FOLLOWS.asInt()); // mark the beginning of command bytes
            outputStream.write(COMMAND_ONE_TWENTY_NINE);                // 0x81 - not sure what this is
            outputStream.write(command.asInt());                        // the actual command
            outputStream.write(dataByte.asInt());                       // sometimes a particular byte must follow the command, but not always
            outputStream.write(CMS50FWCommand.PADDING.asInt());
            outputStream.write(CMS50FWCommand.PADDING.asInt());
            outputStream.write(CMS50FWCommand.PADDING.asInt());
            outputStream.write(CMS50FWCommand.PADDING.asInt());
            outputStream.write(CMS50FWCommand.PADDING.asInt());
            outputStream.flush();
        } else {
            Log.w(TAG, COULD_NOT_WRITE_COMMAND_MESSAGE);
        }
    }

    /**
     * Use this instead of {@link #writeCommand(CMS50FWCommand, CMS50FWCommand)} if there
     * is no additional data following the command.
     *
     * @param command a single command from the custom enum {@link com.albertcbraun.cms50fwlib.CMS50FWCommand}
     * @throws IOException if the write attempt fails and the command is not written back to the device
     *          (because, for example, of a broken Bluetooth connection caused by a Bluetooth device
     *          which has been shut down or moved out of Bluetooth range).
     */
    void writeCommand(CMS50FWCommand command) throws IOException {
        writeCommand(command, CMS50FWCommand.PADDING);
    }

    /**
     * Cancel bluetooth device discovery on this android device if possible.
     */
    void cancelDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    /**
     * Bring the Bluetooth plumbing back to the state it was in before the
     * CMS50FW device was discovered and a connection to it was opened.
     */
    void reset() {
        logEvent(STARTING_RESET_MESSAGE);
        okToReadData = false;

        cancelDiscovery();

        logEvent(CLOSING_BLUETOOTH_SOCKET_AND_IO_STREAMS_MESSAGE);
        if (bluetoothAdapter.isEnabled() && bluetoothSocket != null && bluetoothSocket.isConnected()) {
            close(outputStream, OUTPUT_STREAM);
            outputStream = null;
            close(inputStream, INPUT_STREAM);
            inputStream = null;
            close(bluetoothSocket, BLUETOOTH_SOCKET);
            bluetoothSocket = null;
        }

        cms50FWDevice = null;
        cms50FWConnectionListener.onConnectionReset();

        logEvent(RESET_COMPLETE_MESSAGE);
    }

    /**
     * Close an IO stream or socket.
     *
     * @param objectRef the stream or socket
     * @param objectName the name, for logging message purposes
     */
    private void close(Closeable objectRef, String objectName) {
        if (objectRef != null) {
            try {
                objectRef.close();
                logEvent(Util.formatString(CLOSED_FORMAT_STRING, objectName));
            } catch (IOException e) {
                Log.e(TAG, Util.formatString(COULD_NOT_CLOSE_FORMAT_STRING, objectName), e);
            }
        }
    }

    /**
     * Allows the BroadcastReceiver to unregister itself after it has completed
     * its work.
     *
     * @param context a {@link Context} used temporarily and then discarded
     */
    private void unregisterBroadcastReceiver(Context context) {
        if (broadcastReceiver != null && broadcastReceiverIsRegistered) {
            try {
                context.unregisterReceiver(broadcastReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, COULD_NOT_UNREGISTER_BROADCAST_RECEIVER_PROCEEDING_ANYWAY_MESSAGE);
            }
            broadcastReceiverIsRegistered = false;
        }
    }

    /**
     * This does the real work of finding, connecting to the CMS50FW Bluetooth device, and
     * opening sockets and streams. This is invoked when the system creates a broadcast
     * in response to a successful Bluetooth discovery command.
     */
    private class CMS50FWBroadcastReceiver extends BroadcastReceiver {

        private static final String ATTEMPTING_TO_CONNECT_TO_CMS50FW_MESSAGE = "Attempting to connect to CMS50FW.";
        private static final String RETRIEVING_UUIDS_FROM_BLUETOOTH_DEVICE_FORMAT = "Retrieving UUIDs from BluetoothDevice: Name:%s, Address:%s, BluetoothClass:%s";
        private static final String RETRIEVED_UUID_FROM_CMS50_FW_WILL_USE_MESSAGE = "Retrieved UUID from CMS50FW. Will use this UUID instead of default:";
        private static final String ATTEMPTING_TO_GET_NEW_BLUETOOTH_SOCKET_TO_CMS50_FW_DEVICE_MESSAGE = "Attempting to get new bluetoothSocket to CMS50FW device";
        private static final String ATTEMPTING_TO_CONNECT_ON_BLUETOOTH_SOCKET_MESSAGE = "Attempting to connect on bluetoothSocket";
        private static final String BLUETOOTH_SOCKET_CONNECTED_SUCCESSFULLY_MESSAGE = "BluetoothSocket connected successfully.";
        private static final String SET_REFERENCES_TO_INPUT_AND_OUTPUT_STREAMS_MESSAGE = "Set references to input and output streams.";
        private static final String DISCOVERY_AND_CONNECTION_COMPLETE_MESSAGE = "Discovery and connection complete.";
        private static final String IO_EXCEPTION_TRYING_TO_GET_AND_CONNECT_BLUETOOTH_SOCKET_MESSAGE = "IOException trying to get and connect BluetoothSocket";
        private static final String ERROR_CONNECT_ATTEMPT_FAILED_PLEASE_TRY_AGAIN_MESSAGE = "Error: connect attempt failed. Please try again.";
        private static final String A_BLUETOOTH_DEVICE_HAS_BEEN_FOUND_MESSAGE = "A Bluetooth device has been found.";
        private static final String BLUETOOTH_DEVICE_FOUND_FORMAT = "BluetoothDevice found: Name:%s, Address:%s, BluetoothClass:%s";
        private static final String CMS50FW_BLUETOOTH_DEVICE_FOUND_MESSAGE = "The Bluetooth device found is the CMS50FW";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                logEvent(A_BLUETOOTH_DEVICE_HAS_BEEN_FOUND_MESSAGE);
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // BluetoothDevice found: Name:SpO202, Address:00:0E:..., BluetoothClass:1f00
                Log.v(TAG, Util.formatString(BLUETOOTH_DEVICE_FOUND_FORMAT,
                        device.getName(), device.getAddress(), device.getBluetoothClass()));
                if (device.getName() != null && device.getName().equals(androidBluetoothDeviceName)) {
                    logEvent(CMS50FW_BLUETOOTH_DEVICE_FOUND_MESSAGE);
                    cms50FWDevice = device;

                    // we've found our device. so no  more need for discovery. save radio resources!
                    bluetoothAdapter.cancelDiscovery();

                    // TODO: reinstate and test on appropriate devices, both above and below API 19
                    // Problem: I'm not certain that all CMS50FWs in the field share this Bluetooth PIN.
                    // Requires API 19, so verify the API level here.
                    // byte[] pinCode = new byte[]{7,7,6,2};
                    // cms50FWDevice.setPin(pinCode);

                    if (!connectionAlive()) {
                        logEvent(ATTEMPTING_TO_CONNECT_TO_CMS50FW_MESSAGE);
                        cms50FWConnectionListener.onConnectionAttemptInProgress();

                        Log.v(TAG, Util.formatString(RETRIEVING_UUIDS_FROM_BLUETOOTH_DEVICE_FORMAT,
                                cms50FWDevice.getName(), cms50FWDevice.getAddress(), cms50FWDevice.getBluetoothClass()));

                        // update the UUID with the one from the actual, physical device, if available
                        ParcelUuid[] uuidArray = cms50FWDevice.getUuids();
                        if (uuidArray != null) {
                            for (int i = 0; i < uuidArray.length; i++) {
                                if (i == 0 && uuidArray.length > 0) {
                                    // assume 0th uuid is the uuid for the service we want
                                    bluetoothServiceUUID = uuidArray[i].getUuid();
                                    Log.v(TAG, RETRIEVED_UUID_FROM_CMS50_FW_WILL_USE_MESSAGE +
                                            uuidArray[i].getUuid());
                                }
                            }
                        }

                        // get socket and connect
                        try {
                            logEvent(ATTEMPTING_TO_GET_NEW_BLUETOOTH_SOCKET_TO_CMS50_FW_DEVICE_MESSAGE);
                            bluetoothSocket = cms50FWDevice.createRfcommSocketToServiceRecord(bluetoothServiceUUID);
                            logEvent(ATTEMPTING_TO_CONNECT_ON_BLUETOOTH_SOCKET_MESSAGE);
                            bluetoothSocket.connect();
                            logEvent(BLUETOOTH_SOCKET_CONNECTED_SUCCESSFULLY_MESSAGE);
                            inputStream = bluetoothSocket.getInputStream();
                            outputStream = bluetoothSocket.getOutputStream();
                            logEvent(SET_REFERENCES_TO_INPUT_AND_OUTPUT_STREAMS_MESSAGE);
                            cms50FWConnectionListener.onConnectionEstablished();
                            logEvent(DISCOVERY_AND_CONNECTION_COMPLETE_MESSAGE);
                        } catch (IOException e) {
                            Log.e(TAG, IO_EXCEPTION_TRYING_TO_GET_AND_CONNECT_BLUETOOTH_SOCKET_MESSAGE, e);
                            logEvent(ERROR_CONNECT_ATTEMPT_FAILED_PLEASE_TRY_AGAIN_MESSAGE);
                        }

                        // remove this broadcast receiver right away. we don't want it hanging around
                        // because we do not have an easy way to unregister it later (unless we hold
                        // a reference to a Context, which we do not want to do.)
                        unregisterBroadcastReceiver(context);
                    }
                }
            }
        }
    }

}
