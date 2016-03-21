package com.bopr.android.smailer.ui;


import android.support.annotation.NonNull;

/**
 * For debug purposes.
 */
public class DebugSettingsActivity extends DefaultActivity {

    @NonNull
    @Override
    protected DebugFragment createFragment() {
        setClosable(true);
        return new DebugFragment();
    }

}
