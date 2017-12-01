package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Recipients list activity.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RecipientsActivity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setClosable(true);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new RecipientsFragment();
    }

}