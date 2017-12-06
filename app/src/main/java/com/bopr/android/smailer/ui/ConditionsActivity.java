package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Conditions settings activity.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ConditionsActivity extends AppActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new ConditionsFragment();
    }

}
