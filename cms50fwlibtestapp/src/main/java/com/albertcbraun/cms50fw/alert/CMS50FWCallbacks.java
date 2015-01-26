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

import android.widget.Button;

import com.albertcbraun.cms50fwlib.CMS50FWConnectionListener;
import com.albertcbraun.cms50fwlib.DataFrame;

/**
 * Implementation of the listener interface which
 * contains all the client app callbacks required by
 * the cms50fwlib library.
 * <p/>
 * Created by albertb on 1/10/2015.
 */
class CMS50FWCallbacks implements CMS50FWConnectionListener {

    private static final String TAG = CMS50FWCallbacks.class.getSimpleName();
    private static final String BROKEN_CONNECTION_MESSAGE = "Not currently connected to CMS50FW over Bluetooth. " +
            "Please verify that CMS50FW is on and within Bluetooth range. " +
            "It may also be necessary to click the reset button, connect button, and start data buttons again. " +
            "It may even be necessary to restart the app.";

    private MainUIFragment mainUIFragment = null;
    private Button initializeButton = null;
    private Button startDataButton = null;
    private Button stopDataButton = null;

    CMS50FWCallbacks(final MainUIFragment mainUIFragment,
                     Button initializeButton, Button startDataButton, Button stopDataButton) {
        this.mainUIFragment = mainUIFragment;
        this.initializeButton = initializeButton;
        this.startDataButton = startDataButton;
        this.stopDataButton = stopDataButton;
    }

    @Override
    public void onLogEvent(long timeMs, String message) {
        mainUIFragment.writeMessage(timeMs, message);
    }

    @Override
    public void onDataReadAttemptInProgress() {
        startDataButton.setEnabled(false);
        stopDataButton.setEnabled(true);
    }

    @Override
    public void onDataReadStopped() {
        startDataButton.setEnabled(true);
        stopDataButton.setEnabled(false);

    }

    @Override
    public void onBrokenConnection() {
        mainUIFragment.setUIAlert(BROKEN_CONNECTION_MESSAGE);
        mainUIFragment.writeMessage(System.currentTimeMillis(), BROKEN_CONNECTION_MESSAGE);
    }

    @Override
    public void onConnectionEstablished() {
        startDataButton.setEnabled(true);
    }

    @Override
    public void onConnectionReset() {
        initializeButton.setEnabled(true);
        startDataButton.setEnabled(false);
        stopDataButton.setEnabled(false);
    }

    @Override
    public void onConnectionAttemptInProgress() {
        initializeButton.setEnabled(false);
    }

    @Override
    public void onDataFrameArrived(DataFrame dataFrame) {
        mainUIFragment.processDataFrame(dataFrame);
    }
}
