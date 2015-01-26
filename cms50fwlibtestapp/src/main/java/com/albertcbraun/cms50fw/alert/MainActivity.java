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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;

/**
 * This class exists to forward requests to the MainUIFragment, 
 * and to otherwise make it possible to use a Fragment.
 * We want to take advantage of the Fragment's ability to 
 * be retained even when an Activity is stopped or destroyed
 * by the system during a resource reconfiguration and reload event.
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        new SimpleEula(this).show(savedInstanceState);
    }

    MainUIFragment findMyFragment() {
        return (MainUIFragment) getSupportFragmentManager().findFragmentByTag(MainUIFragment.TAG);
    }

    public void connect(View v) {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.connect(v);
        }
    }

    public void resetState(View v) {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.resetState(v);
        }
    }

    public void startData(View v) {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.startData(v);
        }
    }

    public void stopData(View v) {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.stopData(v);
        }
    }

    public void clearWindow(View v) {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.clearWindow(v);
        }
    }

    void stopAlertSound() {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.stopAlertSound();
        }
    }

    void disableAlertSound() {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.disableAlertSound();
        }
    }

}
