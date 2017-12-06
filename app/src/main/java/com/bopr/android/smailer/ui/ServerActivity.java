package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Outgoing server settings activity.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ServerActivity extends AppActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new ServerFragment();
    }

}
