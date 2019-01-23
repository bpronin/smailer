package com.bopr.android.smailer.ui;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * Recipients list activity.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RecipientsActivity extends AppActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new RecipientsFragment();
    }

}