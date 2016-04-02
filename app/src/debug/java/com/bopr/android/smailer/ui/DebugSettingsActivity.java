package com.bopr.android.smailer.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * For debug purposes.
 */
public class DebugSettingsActivity extends DefaultActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setClosable(true);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected DebugFragment createFragment() {
        return new DebugFragment();
    }

}
