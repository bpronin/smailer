package com.bopr.android.smailer.ui;

import android.os.Bundle;

import com.bopr.android.smailer.ContentObserverService;
import com.bopr.android.smailer.RemoteControlWorker;
import com.bopr.android.smailer.ResendWorker;
import com.bopr.android.smailer.Settings;
import com.crashlytics.android.Crashlytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import io.fabric.sdk.android.Fabric;

import static com.bopr.android.smailer.util.Util.registerUncaughtExceptionHandler;

/**
 * An activity that presents a set of application settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainActivity extends AppActivity {

    private static Logger log = LoggerFactory.getLogger("MainActivity");

    static {
        registerUncaughtExceptionHandler();
    }

    public MainActivity() {
        setClosable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.debug("Application init");
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());
        Settings.init(this);
        ContentObserverService.enable(this);
        ResendWorker.enable(this);
        RemoteControlWorker.enable(this);
    }

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }

}
