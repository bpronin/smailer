package com.bopr.android.smailer.ui;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * An activity that presents an application activity log.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class LogActivity extends AppActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new LogFragment();
    }

}
