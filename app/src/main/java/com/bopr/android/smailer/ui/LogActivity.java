package com.bopr.android.smailer.ui;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * An activity that presents an application activity log.
 */
public class LogActivity extends DefaultActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setClosable(true);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new LogFragment();
    }

}
