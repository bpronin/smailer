package com.bopr.android.smailer.ui;


import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * Server settings activity.
 */
public class ServerSettingsActivity extends DefaultActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        setClosable(true);
        return new ServerSettingsFragment();
    }

}
