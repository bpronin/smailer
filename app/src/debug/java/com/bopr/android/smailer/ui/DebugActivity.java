package com.bopr.android.smailer.ui;


import androidx.annotation.NonNull;

/**
 * For debug purposes.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class DebugActivity extends BaseActivity {

    @NonNull
    @Override
    protected DebugFragment createFragment() {
        return new DebugFragment();
    }

}
