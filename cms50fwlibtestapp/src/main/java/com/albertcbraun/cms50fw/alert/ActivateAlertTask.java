package com.albertcbraun.cms50fw.alert;

import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;

/**
 * Creates a dialog and raises a distinctive alarm so that the user
 * becomes aware that there is an important problem.
 * <p/>
 * Created by albertb on 1/15/2015.
 */
class ActivateAlertTask implements Runnable {

    private static final int ALERT_DIALOG_ANIMATION_DURATION_MILLIS = 1000;
    private static final AlphaAnimation ALERT_DIALOG_FLASH_ANIMATION;
    private static final float STARTING_ALPHA = 0.0f;
    private static final float ENDING_ALPHA = 1.0f;
    static {
        ALERT_DIALOG_FLASH_ANIMATION = new AlphaAnimation(STARTING_ALPHA, ENDING_ALPHA);
        ALERT_DIALOG_FLASH_ANIMATION.setDuration(ALERT_DIALOG_ANIMATION_DURATION_MILLIS);
        ALERT_DIALOG_FLASH_ANIMATION.setRepeatCount(AlphaAnimation.INFINITE);
        ALERT_DIALOG_FLASH_ANIMATION.setFillAfter(true);
    }
    private static final String TAG = ActivateAlertTask.class.getSimpleName();
    private static final int LEFT_VOLUME = 1;
    private static final int RIGHT_VOLUME = 1;
    private static final int PRIORITY = 0;
    private static final int LOOP_SETTING = -1;
    private static final int RATE = 1;
    private static final String ALERT_SOUND_COULD_NOT_BE_LOADED = "Alert sound was not loaded by SoundPool! No alert sound can be played!";
    private MainUIFragment mainUIFragment = null;
    private String alertMessage = null;

    public ActivateAlertTask(MainUIFragment mainUIFragment, String alertMessage) {
        this.mainUIFragment = mainUIFragment;
        this.alertMessage = alertMessage;
    }

    @Override
    public void run() {
        android.support.v4.app.FragmentActivity activity = mainUIFragment.getActivity();
        if (!mainUIFragment.uiAlertSet) {
            mainUIFragment.uiAlertSet = true;

            // animate the background
            View underlyingView = mainUIFragment.getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
            underlyingView.setBackgroundColor(mainUIFragment.getActivity().getResources().getColor(android.R.color.holo_red_dark));
            underlyingView.startAnimation(ALERT_DIALOG_FLASH_ANIMATION);

            // show problem dialog
            UIAlertDialog uiAlertDialog = new UIAlertDialog();
            Bundle arguments = new Bundle();
            arguments.putString(UIAlertDialog.ALERT_MESSAGE_KEY, alertMessage);
            uiAlertDialog.setArguments(arguments);

            // create dialog box and play a sound
            mainUIFragment.getFragmentManager().beginTransaction().add(uiAlertDialog, UIAlertDialog.TAG).commitAllowingStateLoss();
            if (mainUIFragment.alertSoundEnabled) {
                final int soundId = mainUIFragment.soundPool.load(activity.getApplicationContext(), R.raw.beep, 1);
                mainUIFragment.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        if (status == 0) { // sound loaded successfully
                            mainUIFragment.soundStreamId = soundPool.play(soundId, LEFT_VOLUME, RIGHT_VOLUME, PRIORITY, LOOP_SETTING, RATE);
                        } else {
                            Log.e(TAG, ALERT_SOUND_COULD_NOT_BE_LOADED);
                        }
                    }
                });
            }
        }
    }

}
