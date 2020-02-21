package com.bopr.android.smailer.ui;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bopr.android.smailer.Settings;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.bopr.android.smailer.ContentObserverService.enableContentObserverService;
import static com.bopr.android.smailer.Environment.setupEnvironment;
import static com.bopr.android.smailer.ResendWorker.setupResendWorker;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.sync.SyncEngine.onSyncSettingsChanged;

/**
 * An activity that presents a set of application settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainActivity extends AppActivity implements OnSharedPreferenceChangeListener {

    private BackupManager backupManager;
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHomeButtonEnabled(false);

        backupManager = new BackupManager(this);

        settings = new Settings(this);
        settings.loadDefaults();
        settings.registerChangeListener(this);

        setupEnvironment(this);
        handleStartupParams(getIntent());
    }

    @Override
    protected void onDestroy() {
        settings.unregisterChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PREF_EMAIL_TRIGGERS:
                enableContentObserverService(this);
                break;
            case PREF_RESEND_UNSENT:
                setupResendWorker(this);
                break;
        }

        backupManager.dataChanged();
        onSyncSettingsChanged(this, key);
    }

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    private void handleStartupParams(Intent intent) {
        String screen = intent.getStringExtra("screen");
        if (screen != null) {
            switch (screen) {
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
