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
