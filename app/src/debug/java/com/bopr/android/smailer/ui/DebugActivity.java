package com.bopr.android.smailer.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * For debug purposes.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class DebugActivity extends BaseActivity {

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
