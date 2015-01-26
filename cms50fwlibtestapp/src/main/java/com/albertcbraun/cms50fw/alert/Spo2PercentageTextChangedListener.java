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
