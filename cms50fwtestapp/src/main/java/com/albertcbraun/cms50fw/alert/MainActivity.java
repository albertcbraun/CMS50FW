package com.albertcbraun.cms50fw.alert;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainUIFragment(), MainUIFragment.TAG)
                    .commit();
        }
    }

    MainUIFragment findMyFragment() {
        return (MainUIFragment) getSupportFragmentManager().findFragmentByTag(MainUIFragment.TAG);
    }

    public void connect(View v) {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.connect(v);
        }
    }

    public void resetState(View v) {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.resetState(v);
        }
    }

    public void startData(View v) {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.startData(v);
        }
    }

    public void stopData(View v) {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.stopData(v);
        }
    }

    public void clearWindow(View v) {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.clearWindow(v);
        }
    }

    void stopAlertSound() {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.stopAlertSound();
        }
    }

    void disableAlertSound() {
        MainUIFragment f = findMyFragment();
        if (f != null) {
            f.disableAlertSound();
        }
    }

}
