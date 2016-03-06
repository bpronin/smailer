package com.bopr.android.smailer.settings;


import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * Email content activity.
 */
public class EmailContentSettingsActivity extends DefaultPreferenceActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        setClosable(true);
        return new EmailContentSettingsFragment();
    }

}
