package com.bopr.android.smailer;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.bopr.android.smailer.Settings.KEY_PREF_REMOTE_CONTROL_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_REMOTE_CONTROL_ENABLED;
import static com.bopr.android.smailer.Settings.settings;

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
            RemoteControlService.start(context);
        }
        return Result.success();
    }

    private static boolean isFeatureEnabled(@NonNull Context context) {
        return settings(context).getBoolean(KEY_PREF_REMOTE_CONTROL_ENABLED, false);
    }

    public static void enable(@NonNull Context context) {
        WorkManager manager = WorkManager.getInstance();

        manager.cancelAllWorkByTag(WORKER_TAG);

        if (isFeatureEnabled(context) && !settings(context).isNull(KEY_PREF_REMOTE_CONTROL_ACCOUNT)) {
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
