package com.albertcbraun.cms50fw.alert;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * A specialized dialog which demands the attention of the app user.
 * Offers user a button to dismiss both the annoying alarm sound, and
 * the dialog itself.
 * <p/>
 * Created by albertb on 1/2/2015.
 */
public class UIAlertDialog extends android.support.v4.app.DialogFragment {

    public static final String TAG = UIAlertDialog.class.getSimpleName();
    static final String ALERT_MESSAGE_KEY = "ALERT_MESSAGE_KEY";
    private static final String ALERT_DIALOG_TITLE = "Problem Detected";
    private static final String DISMISS_DIALOG_BUTTON_TEXT = "Silence Alarm";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String alertMessage = args.getString(ALERT_MESSAGE_KEY);
        final MainActivity activity = (MainActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_HOLO_DARK);
        builder.setMessage(alertMessage)
                .setTitle(ALERT_DIALOG_TITLE)
                .setPositiveButton(DISMISS_DIALOG_BUTTON_TEXT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.stopAlertSound();
                        activity.disableAlertSound();
                    }
                });
        return builder.create();
    }
}
