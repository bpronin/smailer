package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

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
