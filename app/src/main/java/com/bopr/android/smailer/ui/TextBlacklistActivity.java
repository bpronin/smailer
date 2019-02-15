package com.bopr.android.smailer.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Number blacklist activity.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class TextBlacklistActivity extends AppActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new TextBlacklistFragment();
    }

}