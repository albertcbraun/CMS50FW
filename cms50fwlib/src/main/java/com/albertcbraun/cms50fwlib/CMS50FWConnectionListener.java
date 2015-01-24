package com.albertcbraun.cms50fwlib;

/**
 * This is the set of callbacks this library will use to communicate
 * with your app. Your app must use a custom implementation of
 * this interface to use this library.
 * <p>
 * This callback allows various worker threads to tell your app when
 * important events such as initialization readiness, connection establishment,
 * and data frame arrival.
 * <p/>
 * Created by albertb on 1/11/2015.
 */
public interface CMS50FWConnectionListener {
    /**
     * Attempting to connect over bluetooth socket.
     */
    public void onConnectionAttemptInProgress();

    /**
     * Bluetooth connection on bluetooth socket succeeded.
     */
    public void onConnectionEstablished();

    /**
     * System has started to try reading data from CMS50FW.
     * (The success of reading will be confirmed when the first {@link DataFrame}
     * object is handed to {@link #onDataFrameArrived(DataFrame)}).
     */
    public void onDataReadAttemptInProgress();

    /**
     * A set of data representing one 60Hz data collection
     * cycle has arrived.
     *
     * @param dataFrame the set of data measurements output by one cycle of the CMS50FW
     */
    public void onDataFrameArrived(DataFrame dataFrame);

    /**
     * System has stopped reading data from CMS50FW as the
     * result of a stop data command being sent to the CMS50FW.
     */
    public void onDataReadStopped();

    /**
     * Bluetooth connection to CMS50FW has failed. CMS50FW may,
     * for example, have been turned off or moved out of
     * Bluetooth range.
     */
    public void onBrokenConnection();

    /**
     * Successful cancellation of bluetooth discovery, executor service shutdown, and
     * IO stream closures have occurred.
     */
    public void onConnectionReset();

    /**
     * Log a timestamped message from within the library.
     *
     * @param timeMs time stamp
     * @param message a message which the client or end user may wish to see logged
     */
    public void onLogEvent(long timeMs, String message);

}
