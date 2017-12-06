package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * An activity that shows legal info.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class LegalInfoActivity extends AppActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new LegalInfoFragment();
    }

}
