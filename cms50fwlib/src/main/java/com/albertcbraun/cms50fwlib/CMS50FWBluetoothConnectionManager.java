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

import android.content.Context;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The main entry point to this library.
 * <p>
 * Offers methods which allow your app to issue commands to the
 * Bluetooth connection related objects which run on separate worker
 * threads.
 * <p>
 * All tasks and task executors are managed by this class.
 * <p>
 * The constructor for this class will need the CMS50FW's Android bluetooth device name,
 * which, in my experience is: SpO202
 * <p>
 * (You should be able to read the CMS50FW's bluetooth device name string on your Android phone or tablet's Bluetooth
 * settings if you create a Bluetooth connection to the CMS50FW manually).
 * <p>
 * The client app will also most likely want a custom implementation of {@link com.albertcbraun.cms50fwlib.CMS50FWConnectionListener}.
 * Once that is implemented, instantiate it and set it with {@link #setCMS50FWConnectionListener(CMS50FWConnectionListener)} possibly
 * in onCreate, or onCreateView in your fragment. This is done separately from the constructor so
 * that you are free to construct {@link com.albertcbraun.cms50fwlib.CMS50FWBluetoothConnectionManager}
 * early in the lifecycle, and still have a chance to construct {@link com.albertcbraun.cms50fwlib.CMS50FWConnectionListener}
 * later in the lifecycle, when the UI views have been built and are available to reference.
 * <p>
 * Also note that the CMS50FW usually (always?) has a Bluetooth PIN of 7762. The user of your app
 * will need to enter this PIN manually the first time your Android device tries to Bluetooth connect to the
 * CMS50FW device. The user should be prompted for it by the Android device.
 * <p>
 * Once you have an instance of {@link com.albertcbraun.cms50fwlib.CMS50FWBluetoothConnectionManager},
 * your app will need to call {@link #connect(android.content.Context)} and  wait to be called back
 * through {@link CMS50FWConnectionListener#onConnectionEstablished()}. Once that happens, the
 * Bluetooth connection has been established and your app can call {@link #startData()}.
 * <p>
 * As data comes back from the CMS50FW, additional callbacks should occur about 60 times per second through
 * {@link com.albertcbraun.cms50fwlib.CMS50FWConnectionListener#onDataFrameArrived(DataFrame)}.
 * These {@link DataFrame} objects contain the actual measurement data from the CMS50FW.
 * <p>
 * Later, to stop the flow of data, your app should call {@link #stopData()} and wait for the callback
 * {@link CMS50FWConnectionListener#onDataReadStopped()}.
 * <p>
 * If the Bluetooth plumbing on your Android device gets stuck or seems to be in a confused state,
 * your app should call {@link #reset()}. After that, the app will again need to call
 * {@link #connect(android.content.Context)}.
 * <p>
 * It's a good idea to call {@link #dispose(android.content.Context)} when your app is destroyed so that it can
 * unregister the internal BroadcastReceiver which is created and used inside this library.
 * <p>
 * In addition to those mentioned above, the other callback methods in {@link com.albertcbraun.cms50fwlib.CMS50FWConnectionListener}
 * may prove useful to your app.
 * <p>
 * <b>Please note:</b> at this time, the library does not accurately report pulse values above 127 BPM. It should not be
 * relied upon in such cases.
 * <p>
 * Created by albertb on 1/11/2015.
 */
public class CMS50FWBluetoothConnectionManager {

    private static final String TAG = CMS50FWBluetoothConnectionManager.class.getSimpleName();
    private static final int STAY_CONNECTED_PERIOD_SEC = 5;

    private AndroidBluetoothConnectionComponents androidBluetoothConnectionComponents = null;
    private CMS50FWConnectionListener cms50FWConnectionListener = null;
    private boolean keepAliveTaskRunning;

    // They don't all have to be scheduled ExecutorServices but I
    // made them all the same for simplicity and consistency
    private ScheduledExecutorService generalPurposeExecutor = null;     // runs ResetTask and StopDataTask
    private ScheduledExecutorService readDataExecutor = null;           // runs and re-runs StartDataTask in an indefinite loop
    private ScheduledExecutorService keepAliveExecutor = null;          // runs KeepAliveTask every 5 minutes

    /**
     * Main constructor. You need an instance of this object in order to use
     * this library.
     *
     * @param bluetoothName try using: SpO202
     */
    public CMS50FWBluetoothConnectionManager(String bluetoothName) {
        this.cms50FWConnectionListener = new CMS50FWConnectionLogger();
        this.androidBluetoothConnectionComponents = new AndroidBluetoothConnectionComponents(this,
                this.cms50FWConnectionListener, bluetoothName);
    }

    /**
     * Set the custom instance of {@link com.albertcbraun.cms50fwlib.CMS50FWConnectionListener} for your
     * app here. This is really useful because it informs your app about the state
     * of the bluetooth adapter, connection, progress reading data, etc.
     *
     * @param cms50FWConnectionListener a custom implementation
     *                                  of {@link com.albertcbraun.cms50fwlib.CMS50FWConnectionListener}
     */
    public void setCMS50FWConnectionListener(CMS50FWConnectionListener cms50FWConnectionListener) {
        this.cms50FWConnectionListener = new ConnectionListenerForwarder(cms50FWConnectionListener);
        this.androidBluetoothConnectionComponents.setCms50FWConnectionListener(this.cms50FWConnectionListener);
    }

    /**
     * Most methods create tasks which are run and executed on
     * various executors. These methods are typically invoked from
     * the UI thread. A general rule in these methods is to shutdown executors
     * in the UI thread, but then submit Bluetooth component altering tasks to a
     * worker thread.
     **/

    /**
     * Invoke Bluetooth discovery, wait for it to finish, and then obtain a
     * Bluetooth socket and connect to the main Bluetooth service on the CMS50FW bluetooth device. Also
     * obtains IO streams. (These Bluetooth plumbing details are handled internally
     * so that you do not have to be aware of them.) After a successful
     * connection, as indicated by the callback {@link CMS50FWConnectionListener#onConnectionEstablished()},
     * your app can call {@link #startData()}. This occurs on the UI thread.
     */
    public void connect(Context context) throws BluetoothNotAvailableException, BluetoothNotEnabledException{
        androidBluetoothConnectionComponents.findAndConnect(context);
    }

    /**
     * Request data from the CMS50FW by issuing a start command on the
     * input stream. Also start the keep-alive service which pings the
     * CMS50FW every 5 seconds to ensure that its Bluetooth connection
     * remains alive.
     */
    public void startData() {
        androidBluetoothConnectionComponents.okToReadData = true;
        if (keepAliveExecutor == null ||
                keepAliveExecutor.isShutdown() ||
                keepAliveExecutor.isTerminated()) {
            keepAliveExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (!keepAliveTaskRunning) {
            keepAliveExecutor.scheduleAtFixedRate(new KeepAliveTask(androidBluetoothConnectionComponents),
                    0, STAY_CONNECTED_PERIOD_SEC, TimeUnit.SECONDS);
            keepAliveTaskRunning = true;
        }
        if (readDataExecutor == null) {
            readDataExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        readDataExecutor.submit(new StartDataTask(androidBluetoothConnectionComponents));
    }

    /**
     * Ask the CMS50FW to stop sending data by issuing a stop command on the
     * input stream. Also shutdown the keep-alive service.
     */
    public void stopData() {
        Util.safeShutdown(keepAliveExecutor);
        keepAliveTaskRunning = false;
        submitToGeneralExecutor(new StopDataTask(androidBluetoothConnectionComponents));
    }

    /**
     * Stop the data. Cancel discovery if ongoing. Shutdown the keep-alive service. Close IO
     * streams, etc. Resets and/or nullifies the Bluetooth connection and other components related
     * to it.
     * <p/>
     * In order to read data again after this method has been called, {@link #connect(android.content.Context)}
     * must be called again.
     */
    public void reset() {
        stopData();
        submitToGeneralExecutor(new ResetTask(androidBluetoothConnectionComponents));
    }

    /**
     * Shutdown and dispose of the executors and the
     * bluetooth connection manager object.
     */
    public void dispose(Context context) {
        Util.safeShutdown(keepAliveExecutor);
        Util.safeShutdown(readDataExecutor);
        Util.safeShutdown(generalPurposeExecutor);

        // since all executors have been shut down, call dispose on UI thread
        androidBluetoothConnectionComponents.dispose(context);
    }

    private void submitToGeneralExecutor(Runnable task) {
        if (generalPurposeExecutor == null || generalPurposeExecutor.isShutdown() || generalPurposeExecutor.isTerminated()) {
            generalPurposeExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        generalPurposeExecutor.submit(task);
    }
}
