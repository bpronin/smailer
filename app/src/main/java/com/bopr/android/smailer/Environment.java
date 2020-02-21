package com.bopr.android.smailer;

import android.content.Context;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bopr.android.smailer.ContentObserverService.enableContentObserverService;
import static com.bopr.android.smailer.ResendWorker.enableResendWorker;
import static com.bopr.android.smailer.remote.RemoteControlWorker.enableRemoteControlWorker;
import static com.bopr.android.smailer.sync.SyncEngine.startSyncEngine;
import static java.lang.Thread.UncaughtExceptionHandler;
import static java.lang.Thread.getDefaultUncaughtExceptionHandler;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;
import static java.util.Objects.requireNonNull;

public class Environment {

    private static Logger log = LoggerFactory.getLogger("Environment");

    public static void setupEnvironment(Context context) {
        log.debug("Application init");

        setupDefaultExceptionHandler();
        startSyncEngine(context);
        enableContentObserverService(context);
        enableResendWorker(context);
        enableRemoteControlWorker(context);
    }

    private static void setupDefaultExceptionHandler() {
        final UncaughtExceptionHandler defaultHandler = requireNonNull(getDefaultUncaughtExceptionHandler());
        setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                log.error("Application crashed", throwable);
            } catch (Throwable x) {
                Log.e("main", "Failed to handle uncaught exception");
            }
            defaultHandler.uncaughtException(thread, throwable);
        });
    }

}
