package com.bopr.android.smailer.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Phone number blacklist activity.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class PhoneBlacklistActivity extends AppActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new PhoneBlacklistFragment();
    }

}