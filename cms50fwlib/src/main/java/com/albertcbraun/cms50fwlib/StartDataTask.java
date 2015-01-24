package com.albertcbraun.cms50fwlib;

import android.util.Log;

import java.io.IOException;

/**
 * Read data from CMS50FW bluetooth input stream, forming
 * DataFrame objects from each 8 byte sequence.
 * <p>
 * <i><b>
 * Please note that this version of this task does not allow
 * a pulseRate above 127. However, at or below 127, the pulseRate does appear
 * to be accurate. This problem may addressed in a later version.
 * If you know the resolution to this problem, please contribute it.
 * </i></b>
 * <p>
 * Created by albertb on 12/22/2014.
 */
class StartDataTask implements Runnable {

    private static final String TAG = StartDataTask.class.getSimpleName();

    private static final int BIT_0 = 1;
    private static final int BIT_1 = 2;
    private static final int BIT_2 = 4;
    private static final int BIT_3 = 8;
    private static final int BITS_ZERO_TO_THREE = BIT_0 | BIT_1 | BIT_2 | BIT_3;
    private static final int BIT_4 = 16;
    private static final int BIT_5 = 32;
    private static final int BIT_6 = 64;
    private static final int BITS_ZERO_TO_SIX = BIT_0 | BIT_1 | BIT_2 | BIT_3 | BIT_4 | BIT_5 | BIT_6;
    private static final int BIT_7 = 128;
    private static final int SIXTY_FOUR = 64;
    private static final int ONE_TWENTY_SEVEN = 127;
    private static final String COULD_NOT_PUT_STREAMING_DATA_INTO_A_NEW_DATA_FRAME = "Could not put streaming data into a new data frame.";
    private static final String ERROR_CONNECTION_IS_NOT_ALIVE_MESSAGE = "Error. Connection is not alive. ";
    private static final String BEGINNING_DATA_READ_OPERATIONS_MESSAGE = "Beginning data read operations.";
    private static final String IO_EXCEPTION_WITH_INPUT_STREAM_OR_OUTPUT_STREAM_OBJECT_MESSAGE =
            "IOException with InputStream or OutputStream object.";
    private static final String CONNECTION_TASK_COMPLETED_MESSAGE = "Connection completed.";

    private AndroidBluetoothConnectionComponents androidBluetoothConnectionComponents = null;
    private CMS50FWConnectionListener cms50FWConnectionListener = null;

    StartDataTask(AndroidBluetoothConnectionComponents androidBluetoothConnectionComponents) {
        this.cms50FWConnectionListener = androidBluetoothConnectionComponents.getCMS50FWConnectionListener();
        this.androidBluetoothConnectionComponents = androidBluetoothConnectionComponents;
    }

    @Override
    public void run() {

        if (!androidBluetoothConnectionComponents.connectionAlive()) {
            Util.log(cms50FWConnectionListener, ERROR_CONNECTION_IS_NOT_ALIVE_MESSAGE);
            return;
        }

        // allow client to know that work has begun. useful for disabling buttons, etc.
        cms50FWConnectionListener.onDataReadAttemptInProgress();

        // tell the manager it's ok to read data
        androidBluetoothConnectionComponents.okToReadData = true;

        try {
            Util.log(cms50FWConnectionListener, BEGINNING_DATA_READ_OPERATIONS_MESSAGE);

            // writing to input stream in order to issue a command
            androidBluetoothConnectionComponents.writeCommand(CMS50FWCommand.START_DATA);

            while (androidBluetoothConnectionComponents.okToReadData) {
                cms50FWConnectionListener.onDataFrameArrived(getNextDataFrame());
            }
        } catch (IOException ioe) {
            Util.log(cms50FWConnectionListener, IO_EXCEPTION_WITH_INPUT_STREAM_OR_OUTPUT_STREAM_OBJECT_MESSAGE);
            Log.e(TAG, IO_EXCEPTION_WITH_INPUT_STREAM_OR_OUTPUT_STREAM_OBJECT_MESSAGE, ioe);
        } finally {
            Util.log(cms50FWConnectionListener, CONNECTION_TASK_COMPLETED_MESSAGE);
        }

        cms50FWConnectionListener.onDataReadStopped();
    }

    /**
     * Extract the frame of data representing one tick of the 60HZ data stream
     * transmitted via Bluetooth from the CMS50FW.
     *
     * @return a new DataFrame object whose values are derived from seven byte
     * sequences found in the data stream. Each seven byte sequence is
     * preceded by a single boundary byte so that each data frame is considered
     * to be eight bytes long.
     */
    private DataFrame getNextDataFrame() {

        // separates each frame from previous frame
        //noinspection UnusedAssignment
        byte frameBoundary; // aka byte1

        // actual data is stored in these seven bytes
        //noinspection UnusedAssignment
        byte byte2; // ignored
        byte byte3;
        byte byte4;
        byte byte5;
        byte byte6;
        //noinspection UnusedAssignment
        byte byte7; // ignored
        //noinspection UnusedAssignment
        byte byte8; // ignored

        if (androidBluetoothConnectionComponents.inputStream != null) {
            try {
                // create a new empty data frame, ready to be filled in
                DataFrame dataFrame = new DataFrame();

                // search the stream until the byte which signals the beginning of the next data frame is found
                while (true) {
                    byte frameBoundaryCandidate = waitForNextByte();
                    if ((frameBoundaryCandidate & BIT_7) == BIT_7) { // look for next byte with the 7 bit set
                        //noinspection UnusedAssignment
                        frameBoundary = frameBoundaryCandidate;
                        break;
                    }
                }

                // the next 7 bytes are the meaningful ones in the CMS50FW data stream
                // but, in this code, byte2, byte7 and byte8 will not be used

                //noinspection UnusedAssignment
                byte2 = waitForNextByte();

                // bytes we actually use
                byte3 = waitForNextByte();
                byte4 = waitForNextByte();
                byte5 = waitForNextByte();
                byte6 = waitForNextByte();

                //noinspection UnusedAssignment
                byte7 = waitForNextByte();
                //noinspection UnusedAssignment
                byte8 = waitForNextByte();

                dataFrame.pulseWaveForm = (byte3 & BITS_ZERO_TO_SIX);
                dataFrame.pulseIntensity = (byte4 & BITS_ZERO_TO_THREE);
                dataFrame.pulseRate = (byte5 & BITS_ZERO_TO_SIX); // TODO: this does not allow pulseRate to be above 127.  But for lower values, it seems to be correct.
                dataFrame.spo2Percentage = (byte6 & BITS_ZERO_TO_SIX);
                dataFrame.isFingerOutOfSleeve = (dataFrame.pulseWaveForm == SIXTY_FOUR) &&
                        (dataFrame.pulseRate == ONE_TWENTY_SEVEN) && (dataFrame.spo2Percentage == ONE_TWENTY_SEVEN);

                return dataFrame;

            } catch (IOException e) {
                Log.e(TAG, COULD_NOT_PUT_STREAMING_DATA_INTO_A_NEW_DATA_FRAME, e);
            }
        }
        return null;
    }

    /**
     * Spins until a byte becomes available on the input stream. Then it
     * reads and returns that single byte.
     *
     * @return the next byte from the input stream
     * @throws IOException if the Bluetooth connection is unexpectedly closed or
     * the stream can't be read for some reason.
     */
    private byte waitForNextByte() throws IOException {
        // It's important to check for a live connection frequently here because another thread
        // might close connection, causing an IOException to be thrown from this code

        //noinspection StatementWithEmptyBody
        while (androidBluetoothConnectionComponents.connectionAlive() && androidBluetoothConnectionComponents.inputStream.available() <= 0) {
            // do nothing until a byte is available from input stream
        }
        if (androidBluetoothConnectionComponents.connectionAlive()) {
            return (byte) androidBluetoothConnectionComponents.inputStream.read();
        }

        return 0;
    }

}
