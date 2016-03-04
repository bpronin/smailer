package com.bopr.android.smailer.settings;


import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * An activity that presents a set of application settings.
 */
public class MoreSettingsActivity extends DefaultPreferenceActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        setClosable(true);
        return new MoreSettingsFragment();
    }

}
