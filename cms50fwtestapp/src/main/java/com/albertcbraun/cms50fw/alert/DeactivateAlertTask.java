package com.albertcbraun.cms50fw.alert;

import android.view.View;

/**
 * Turns off the dialog box (and sound) created by running the
 * {@link ActivateAlertTask}
 * <p/>
 * Created by albertb on 1/15/2015.
 */
class DeactivateAlertTask implements Runnable {

    private MainUIFragment mainUIFragment = null;

    public DeactivateAlertTask(MainUIFragment mainUIFragment) {
        this.mainUIFragment = mainUIFragment;
    }

    @Override
    public void run() {
        android.support.v4.app.FragmentActivity activity = mainUIFragment.getActivity();
        if (mainUIFragment.uiAlertSet) {
            mainUIFragment.uiAlertSet = false;
            View underlyingView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            underlyingView.clearAnimation();
            underlyingView.setBackgroundColor(activity.getResources().getColor(android.R.color.white));
            android.support.v4.app.FragmentManager fragmentManager = activity.getSupportFragmentManager();
            UIAlertDialog uiAlertDialog = (UIAlertDialog) fragmentManager.findFragmentByTag(UIAlertDialog.TAG);
            if (uiAlertDialog != null) {
                uiAlertDialog.dismissAllowingStateLoss();
            }
            mainUIFragment.stopAlertSound();
            mainUIFragment.enableAlertSound();
        }
    }
}
