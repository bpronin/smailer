package com.bopr.android.smailer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bopr.android.smailer.sync.SyncManager;
import com.crashlytics.android.Crashlytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric.sdk.android.Fabric;

import static com.bopr.android.smailer.util.Util.requireNonNull;
import static java.lang.Thread.UncaughtExceptionHandler;
import static java.lang.Thread.getDefaultUncaughtExceptionHandler;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;

public class Environment {

    private static Logger log = LoggerFactory.getLogger("Environment");

    static {
        final UncaughtExceptionHandler defaultHandler = requireNonNull(getDefaultUncaughtExceptionHandler());
        setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                try {
                    log.error("Application crashed", throwable);
                } catch (Throwable x) {
                    Log.e("main", "Failed to handle uncaught exception");
                }
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }

    public static void setupEnvironment(Context context) {
        log.debug("Application init");

        Fabric.with(context, new Crashlytics());
        AccountsObserver.enable(context);
        ContentObserverService.enable(context);
        ResendWorker.enable(context);
        RemoteControlWorker.enable(context);
        SyncManager.enable(context);
    }

}
