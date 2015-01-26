package com.albertcbraun.cms50fw.alert;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * This is based directly on Donn Felker's Simple EULA class, found at
 * http://www.donnfelker.com/android-a-simple-eula-for-your-android-apps/
 *
 * Minor modifications have been made in order to read the actual text
 * from a file rather than from a string resource.
 * 
 */
public class SimpleEula {

    private static final String TAG = SimpleEula.class.getSimpleName();
    public static final int STRING_BUFFER_CAPACITY = 10000;
    public static final int CHAR_BUFFER_CAPACITY = 512;

    private String EULA_PREFIX = "eula_";
    private ActionBarActivity activity;

    public SimpleEula(ActionBarActivity activity) {
        this.activity = activity;
    }

    private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
            pi = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not get package info", e);
        }
        return pi;
    }

    public void show(Bundle savedInstanceState) {
        PackageInfo versionInfo = getPackageInfo();
        if (versionInfo == null) {
            return;
        }

        // the eulaKey changes every time you increment the version number in the AndroidManifest.xml
        final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean hasBeenShown = prefs.getBoolean(eulaKey, false);
        Log.v(TAG, "hasBeenShown:" + hasBeenShown);
        if(!hasBeenShown){

            // Show the Eula
            String title = activity.getString(R.string.app_name) + " v" + versionInfo.versionName;

            //Includes the updates as well so users know what changed.
            //String message = activity.getString(R.string.eula);
            //AssetManager assetManager = activity.getResources().getAssets();
            String message;
            InputStreamReader inputStreamReader = null;
            try {
                inputStreamReader = new InputStreamReader(activity.getResources().openRawResource(R.raw.eula));
                char[] buf = new char[CHAR_BUFFER_CAPACITY];
                StringBuilder sb = new StringBuilder(STRING_BUFFER_CAPACITY);
                while(inputStreamReader.read(buf) != -1) {
                    sb.append(buf);
                    Arrays.fill(buf, ' '); // clear the buffer
                }
                message = sb.toString();
            } catch (IOException e) {
                Log.e(TAG, "Could not open text asset file: eula.txt", e);
                return;
            } finally {
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close inputStream", e);
                    }
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.i_agree, new Dialog.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Mark this version as read.
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(eulaKey, true);
                            editor.commit();
                            dialogInterface.dismiss();

                            startAvoSelectionActivity(activity);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Close the activity as they have declined the EULA
                            activity.finish();
                        }

                    });
            builder.create().show();
        } else {
            // they must have already agreed to the EULA in the past, so start the next activity
            //startAvoSelectionActivity(activity);

            if (savedInstanceState == null) {
                activity.getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new MainUIFragment(), MainUIFragment.TAG)
                        .commit();
            }

        }
    }

    private void startAvoSelectionActivity(Activity splashActivity) {
        Intent avoSelectionActivityIntent = new Intent();
        avoSelectionActivityIntent.setClass(splashActivity.getApplicationContext(), 
                MainActivity.class);
        splashActivity.startActivity(avoSelectionActivityIntent);
        splashActivity.finish();
    }

}