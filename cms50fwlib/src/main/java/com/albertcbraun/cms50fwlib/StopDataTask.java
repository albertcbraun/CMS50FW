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
 * Writes a {@link com.albertcbraun.cms50fwlib.CMS50FWCommand#STOP_DATA} byte to the
 * CMS50FW so that it stops sending data back to this app.
 * <p>
 * Created by albertb on 1/19/2015.
 */
class StopDataTask implements Runnable {

    private static final String TAG = StopDataTask.class.getSimpleName();
    private static final String WROTE_STOP_DATA_COMMAND_TO_OUTPUT_STREAM = "wrote stop data command to outputStream";
    private static final String COULD_NOT_WRITE_STOP_DATA_COMMAND_TO_OUTPUT_STREAM_MESSAGE = "Could not write stop data command to output stream";
    private static final String OUTPUT_STREAM_IS_NULL_PROBABLY_BEST_TO_RESET_AND_REINITIALIZE_MESSAGE = "Output Stream is null. Probably best to reset and reinitialize.";

    private final AndroidBluetoothConnectionComponents androidBluetoothConnectionComponents;
    private final CMS50FWConnectionListener cms50FWConnectionListener;


    public StopDataTask(AndroidBluetoothConnectionComponents androidBluetoothConnectionComponents) {
        this.androidBluetoothConnectionComponents = androidBluetoothConnectionComponents;
        this.cms50FWConnectionListener = androidBluetoothConnectionComponents.getCMS50FWConnectionListener();
    }

    @Override
    public void run() {
        androidBluetoothConnectionComponents.okToReadData = false;
        if (androidBluetoothConnectionComponents.connectionAlive()) {
            try {
                androidBluetoothConnectionComponents.writeCommand(CMS50FWCommand.STOP_DATA);
                Util.log(cms50FWConnectionListener, WROTE_STOP_DATA_COMMAND_TO_OUTPUT_STREAM);
            } catch (IOException e) {
                Log.e(TAG, COULD_NOT_WRITE_STOP_DATA_COMMAND_TO_OUTPUT_STREAM_MESSAGE, e);
            }
        } else {
            Util.log(cms50FWConnectionListener, OUTPUT_STREAM_IS_NULL_PROBABLY_BEST_TO_RESET_AND_REINITIALIZE_MESSAGE);
        }
    }

}
