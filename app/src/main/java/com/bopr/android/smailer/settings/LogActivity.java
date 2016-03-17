package com.bopr.android.smailer.settings;


import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * An activity that presents an application activity log.
 */
public class LogActivity extends DefaultActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        setClosable(true);
        return new LogFragment();
    }

}
