package com.albertcbraun.cms50fw.alert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Do appropriate stuff when the user changes a SeekBar
 * <p/>
 * Created by albertb on 01/07/2015.
 */
class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = SeekBarChangeListener.class.getSimpleName();

    private Activity activity = null;
    private TextView companionTextView = null;
    private Integer companionInteger = null; // this reference is also accessed from within MainActivity
    private String sharedPreferencesKey = null;

    public SeekBarChangeListener(Activity activity, TextView companionTextView, Integer companionInteger, String sharedPreferencesKey) {
        this.activity = activity;
        this.companionTextView = companionTextView;
        this.companionInteger = companionInteger;
        this.sharedPreferencesKey = sharedPreferencesKey;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        companionTextView.setText(String.format("%d", progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.activity.getApplicationContext());
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
        companionInteger = seekBar.getProgress();
        editor.putInt(sharedPreferencesKey, companionInteger);
        editor.commit();
    }
}
