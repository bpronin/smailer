package com.bopr.android.smailer.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bopr.android.smailer.Environment;

import static com.bopr.android.smailer.Settings.initSettings;

/**
 * An activity that presents a set of application settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainActivity extends AppActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSettings(this);
        Environment.INSTANCE.setupEnvironment(this);
        setHomeButtonEnabled(false);
        handleStartupParams(getIntent());
    }

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private void handleStartupParams(Intent intent) {
        String stringExtra = intent.getStringExtra("screen");
        if (stringExtra != null) {
            switch (stringExtra) {
                case "debug":
                    try {
                        startActivity(new Intent(this, Class.forName("com.bopr.android.smailer.ui.DebugActivity")));
                    } catch (ClassNotFoundException x) {
                        throw new RuntimeException(x);
                    }
                    break;
            }
        }
    }
}
