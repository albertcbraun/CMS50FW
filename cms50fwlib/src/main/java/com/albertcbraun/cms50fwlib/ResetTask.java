package com.albertcbraun.cms50fwlib;

/**
 * Closes the Bluetooth connection to the CMS50FW.
 * Also nullifies the Bluetooth socket, and IO streams.
 * <p>
 * Created by albertb on 1/19/2015.
 */
class ResetTask implements Runnable {

    private AndroidBluetoothConnectionComponents androidBluetoothConnectionComponents = null;

    public ResetTask(AndroidBluetoothConnectionComponents androidBluetoothConnectionComponents) {
        this.androidBluetoothConnectionComponents = androidBluetoothConnectionComponents;
    }

    @Override
    public void run() {
        androidBluetoothConnectionComponents.reset();
    }

}
