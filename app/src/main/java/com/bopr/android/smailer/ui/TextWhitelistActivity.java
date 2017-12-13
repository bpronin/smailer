package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * Text whitelist activity.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class TextWhitelistActivity extends AppActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new TextWhitelistFragment();
    }

}