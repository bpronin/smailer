package com.bopr.android.smailer;

import android.content.Context;

import com.bopr.android.smailer.sync.SyncManager;
import com.crashlytics.android.Crashlytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric.sdk.android.Fabric;

import static com.bopr.android.smailer.util.Util.registerUncaughtExceptionHandler;

public class Environment {

    private static Logger log = LoggerFactory.getLogger("Environment");

    static {
        registerUncaughtExceptionHandler();
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
