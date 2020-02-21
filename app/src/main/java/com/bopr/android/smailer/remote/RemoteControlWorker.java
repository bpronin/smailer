package com.bopr.android.smailer.remote;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_ENABLED;
import static com.bopr.android.smailer.Settings.settings;
import static com.bopr.android.smailer.remote.RemoteControlService.startRemoteControlService;

/**
 * Periodically checks email out for remote tasks.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class RemoteControlWorker extends Worker {

    private static final Logger log = LoggerFactory.getLogger("RemoteControlWorker");

    private static final String WORKER_TAG = "smailer-email";

    public RemoteControlWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        log.debug("Working");

        Context context = getApplicationContext();
        if (isFeatureEnabled(context)) {
            startRemoteControlService(context);
        }
        return Result.success();
    }

    private static boolean isFeatureEnabled(@NonNull Context context) {
        return settings(context).getBoolean(PREF_REMOTE_CONTROL_ENABLED, false);
    }

    public static void enableRemoteControlWorker(@NonNull Context context) {
        WorkManager manager = WorkManager.getInstance();

        manager.cancelAllWorkByTag(WORKER_TAG);

        if (isFeatureEnabled(context)) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(RemoteControlWorker.class,
                    15, TimeUnit.MINUTES) /* interval must be lesser than PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS */
                    .addTag(WORKER_TAG)
                    .setConstraints(constraints)
                    .build();
            manager.enqueueUniquePeriodicWork(WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, request);

            log.debug("Enabled");
        } else {
            log.debug("Disabled");
        }
    }
}
