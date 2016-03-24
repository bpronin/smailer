package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * Class RecipientsSettingsActivity.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RecipientsSettingsActivity extends DefaultActivity{

    @NonNull
    @Override
    protected Fragment createFragment() {
        setClosable(true);
        return new RecipientsFragment();
    }
}