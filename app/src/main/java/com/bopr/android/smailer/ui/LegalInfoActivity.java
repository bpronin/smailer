package com.bopr.android.smailer.ui;


import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * An activity that shows legal info.
 */
public class LegalInfoActivity extends DefaultActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        setClosable(true);
        return new LegalInfoFragment();
    }

}
