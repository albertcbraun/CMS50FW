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
