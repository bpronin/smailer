package com.bopr.android.smailer.ui;


import android.app.Fragment;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.MenuItem;

/**
 * An activity that presents a set of application settings.
 */
public class SettingsActivity extends DefaultActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new SettingsFragment();
    }

    public void onDebugMenuItemClick(MenuItem item) {
        startActivity(new Intent("com.bopr.android.smailer.DEBUG"));
    }
}