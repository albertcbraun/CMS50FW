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
