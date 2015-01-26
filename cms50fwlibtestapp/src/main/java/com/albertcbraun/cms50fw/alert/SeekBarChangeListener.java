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

package com.albertcbraun.cms50fw.alert;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

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
        companionTextView.setText(String.format(Locale.US, "%d", progress));
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
