package com.bopr.android.smailer.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * More settings activity.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MoreActivity extends AppActivity {

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new MoreFragment();
    }

}
