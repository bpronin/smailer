package com.bopr.android.smailer.settings;


import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * Server settings activity.
 */
public class ServerSettingsActivity extends DefaultPreferenceActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        setClosable(true);
        return new ServerSettingsFragment();
    }

}
