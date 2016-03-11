package com.bopr.android.smailer.settings;


import android.support.annotation.NonNull;

/**
 * For debug purposes.
 */
public class DebugSettingsActivity extends DefaultPreferenceActivity {

    @NonNull
    @Override
    protected DebugFragment createFragment() {
        setClosable(true);
        return new DebugFragment();
    }

}
