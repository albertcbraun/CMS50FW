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

import java.io.IOException;

/**
 * This keeps the connection to the CMS50FW alive by pinging it
 * with a specific command byte. (This task is run periodically
 * by an Executor.)
 * <p>
 * Created by albertb on 12/29/2014.
 */
class KeepAliveTask implements Runnable {

    private static final String TAG = KeepAliveTask.class.getSimpleName();

    private static final String BROKEN_PIPE = "Broken pipe";
    private static final String COULD_NOT_WRITE_STAY_CONNECTED_COMMAND_MESSAGE = "Could not " +
            "write stay connected command because socket and/or output stream were not ready.";
    private static final String BROKEN_PIPE_LOG_MESSAGE = "Broken Connection to CMS50FW!";
    private static final String BROKEN_PIPE_COULD_NOT_WRITE_STAY_CONNECTED_COMMAND_MESSAGE = "Could not write stay connected command.";
    private static final String KEEP_ALIVE_TASK_EXITING_WITHOUT_WRITING_CMS50FW_COMMAND_MESSAGE =
            "Keep alive task exiting without writing command to CMS50FW because reading data is not currently enabled.";

    private AndroidBluetoothConnectionComponents androidBluetoothConnectionComponents = null;
    private CMS50FWConnectionListener cms50FWConnectionListener = null;

    public KeepAliveTask(AndroidBluetoothConnectionComponents androidBluetoothConnectionComponents) {
        this.androidBluetoothConnectionComponents = androidBluetoothConnectionComponents;
        this.cms50FWConnectionListener = this.androidBluetoothConnectionComponents.getCMS50FWConnectionListener();
    }

    @Override
    public void run() {
        if (!androidBluetoothConnectionComponents.okToReadData ) {
            Util.log(cms50FWConnectionListener, KEEP_ALIVE_TASK_EXITING_WITHOUT_WRITING_CMS50FW_COMMAND_MESSAGE);
            return;
        }
        if (androidBluetoothConnectionComponents.connectionAlive()) {
            try {
                androidBluetoothConnectionComponents.writeCommand(CMS50FWCommand.STAY_CONNECTED);
            } catch (IOException e) {
                Log.e(TAG, BROKEN_PIPE_COULD_NOT_WRITE_STAY_CONNECTED_COMMAND_MESSAGE, e);
                if (e.getMessage().contains(BROKEN_PIPE)) {
                    Util.log(cms50FWConnectionListener, BROKEN_PIPE_LOG_MESSAGE);
                    cms50FWConnectionListener.onBrokenConnection();
                }
            }
        } else {
            Util.log(cms50FWConnectionListener, COULD_NOT_WRITE_STAY_CONNECTED_COMMAND_MESSAGE);
        }
    }
}
