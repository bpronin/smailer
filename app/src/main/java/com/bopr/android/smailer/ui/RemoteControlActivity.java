package com.bopr.android.smailer.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class RemoteControlActivity extends BaseActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new RemoteControlFragment();
    }
}
