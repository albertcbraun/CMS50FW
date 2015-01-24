package com.albertcbraun.cms50fwlib;

import android.os.Handler;
import android.os.Looper;

/**
 * Ensures that the original CMS50FWConnectionListener instance is
 * called back on the UI thread.
 *
 * Created by albertb on 1/22/2015.
 */
class ConnectionListenerForwarder implements CMS50FWConnectionListener {

    private final String TAG = ConnectionListenerForwarder.class.getSimpleName();

    private CMS50FWConnectionListener cms50FWConnectionListener = null;
    private Handler handler = null;

    public ConnectionListenerForwarder(CMS50FWConnectionListener cms50FWConnectionListener) {
        this.cms50FWConnectionListener = cms50FWConnectionListener;
        this.handler = new Handler(Looper.getMainLooper());
    }

    private void postToUIThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    public void onConnectionAttemptInProgress() {
        postToUIThread(new Runnable() {
            @Override
            public void run() {
                cms50FWConnectionListener.onConnectionAttemptInProgress();
            }
        });
    }

    @Override
    public void onConnectionEstablished() {
        postToUIThread(new Runnable() {
            @Override
            public void run() {
                cms50FWConnectionListener.onConnectionEstablished();
            }
        });
    }

    @Override
    public void onDataReadAttemptInProgress() {
        postToUIThread(new Runnable() {
            @Override
            public void run() {
                cms50FWConnectionListener.onDataReadAttemptInProgress();
            }
        });
    }

    @Override
    public void onDataFrameArrived(final DataFrame dataFrame) {
        postToUIThread(new Runnable() {
            @Override
            public void run() {
                cms50FWConnectionListener.onDataFrameArrived(dataFrame);
            }
        });
    }

    @Override
    public void onDataReadStopped() {
        postToUIThread(new Runnable() {
            @Override
            public void run() {
                cms50FWConnectionListener.onDataReadStopped();
            }
        });
    }

    @Override
    public void onBrokenConnection() {
        postToUIThread(new Runnable() {
            @Override
            public void run() {
                cms50FWConnectionListener.onBrokenConnection();
            }
        });
    }

    @Override
    public void onConnectionReset() {
        postToUIThread(new Runnable() {
            @Override
            public void run() {
                cms50FWConnectionListener.onConnectionReset();
            }
        });
    }

    @Override
    public void onLogEvent(final long timeMs, final String message) {
        postToUIThread(new Runnable() {
            @Override
            public void run() {
                cms50FWConnectionListener.onLogEvent(timeMs, message);
            }
        });
    }
}
