package com.bopr.android.smailer.settings;


import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * Source content activity.
 */
public class SourceSettingsActivity extends DefaultPreferenceActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        setClosable(true);
        return new SourceSettingsFragment();
    }

}
