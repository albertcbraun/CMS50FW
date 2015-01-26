/*
 * Copyright (c) 2015 Albert C. Braun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
