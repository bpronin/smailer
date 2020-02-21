package com.bopr.android.smailer;

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

import static com.bopr.android.smailer.PendingCallProcessorService.startPendingCallProcessorService;
import static com.bopr.android.smailer.Settings.PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.settings;

/**
 * Checks internet connection every 15 minutes and tries to resend email for all pending events.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ResendWorker extends Worker {

    private static final Logger log = LoggerFactory.getLogger("ResendWorker");

    private static final String WORKER_TAG = "smailer-resend";

    public ResendWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        log.debug("Working");

        Context context = getApplicationContext();
        if (isFeatureEnabled(context)) {
            startPendingCallProcessorService(context);
        }
        return Result.success();
    }

    private static boolean isFeatureEnabled(@NonNull Context context) {
        return settings(context).getBoolean(PREF_RESEND_UNSENT, true);
    }

    public static void setupResendWorker(@NonNull Context context) {
        WorkManager manager = WorkManager.getInstance();

        manager.cancelAllWorkByTag(WORKER_TAG);

        if (isFeatureEnabled(context)) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(ResendWorker.class,
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
