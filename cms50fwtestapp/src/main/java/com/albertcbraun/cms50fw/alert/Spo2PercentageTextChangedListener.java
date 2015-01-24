package com.albertcbraun.cms50fw.alert;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Adjusts value of the minimum SpO2 threshold when user sets its value on SeekBar.
 * <p/>
 * Created by albertb on 1/7/2015.
 */
public class Spo2PercentageTextChangedListener implements TextWatcher {

    private MainUIFragment mainUIFragment = null;

    public Spo2PercentageTextChangedListener(MainUIFragment mainUIFragment) {
        this.mainUIFragment = mainUIFragment;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mainUIFragment.minimumSpo2Percentage = Integer.valueOf(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
        // do nothing
    }
}
