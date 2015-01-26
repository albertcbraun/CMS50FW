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

package com.albertcbraun.cms50fwlib;

import android.util.Log;

import java.util.Date;

/**
 * This implementation just logs messages using the standard Android logging
 * mechanism. It is highly recommended that the client app implement its
 * own custom version of {@link com.albertcbraun.cms50fwlib.CMS50FWConnectionListener}
 * instead of relying on this default implementation.
 *
 * Created by albertb on 1/23/2015.
 */
class CMS50FWConnectionLogger implements CMS50FWConnectionListener {

    private static final String TAG = CMS50FWConnectionLogger.class.getSimpleName();

    @Override
    public void onConnectionAttemptInProgress() {
        Log.v(TAG, "ConnectionAttemptInProgress");
    }

    @Override
    public void onConnectionEstablished() {
        Log.v(TAG, "ConnectionEstablished");
    }

    @Override
    public void onDataReadAttemptInProgress() {
        Log.v(TAG, "DataReadAttemptInProgress");
    }

    @Override
    public void onDataFrameArrived(DataFrame dataFrame) {
        Log.v(TAG, "DataFrameArrived:" + dataFrame.toString());
    }

    @Override
    public void onDataReadStopped() {
        Log.v(TAG, "DataReadStopped");
    }

    @Override
    public void onBrokenConnection() {
        Log.v(TAG, "BrokenConnection");
    }

    @Override
    public void onConnectionReset() {
        Log.v(TAG, "ConnectionReset");
    }

    @Override
    public void onLogEvent(long timeMs, String message) {
        Log.v(TAG, Util.formatString("[%s] %s", Util.DATE_FORMAT.format(new Date(timeMs)), message));
    }
}
