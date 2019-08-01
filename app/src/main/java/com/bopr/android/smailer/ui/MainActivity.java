package com.bopr.android.smailer.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bopr.android.smailer.ContentObserverService;
import com.bopr.android.smailer.RemoteControlWorker;
import com.bopr.android.smailer.ResendWorker;
import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.sync.SyncManager;
import com.crashlytics.android.Crashlytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric.sdk.android.Fabric;

import static com.bopr.android.smailer.util.Util.registerUncaughtExceptionHandler;

/**
 * An activity that presents a set of application settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainActivity extends AppActivity {

    static {
        registerUncaughtExceptionHandler();
    }

    private static Logger log = LoggerFactory.getLogger("MainActivity");

    private SyncManager syncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.debug("Application init");
        super.onCreate(savedInstanceState);
        setHomeButtonEnabled(false);
        
        Fabric.with(this, new Crashlytics());
        Settings.init(this);
        ContentObserverService.enable(this);
        ResendWorker.enable(this);
        RemoteControlWorker.enable(this);
        syncManager = new SyncManager(this);

        handleStartupParams(getIntent());
    }

    @Override
    protected void onDestroy() {
        syncManager.dispose();
        super.onDestroy();
    }

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }

    private void handleStartupParams(Intent intent) {
        if (intent.hasExtra("screen")) {
            switch (intent.getStringExtra("screen")) {
                case "debug":
                    try {
                        startActivity(new Intent(this, Class.forName("com.bopr.android.smailer.ui.DebugActivity")));
                    } catch (ClassNotFoundException x) {
                        throw new IllegalArgumentException(x);
                    }
                    break;
            }
        }
    }
}
