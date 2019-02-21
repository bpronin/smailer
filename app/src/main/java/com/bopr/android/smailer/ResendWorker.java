package com.bopr.android.smailer;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;

/**
 * Checks internet connection every 5 minutes and tries to resend email for all pending events.
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
        log.debug("Executing");
        Context context = getApplicationContext();
        if (isResendEnabled(context)) {
            CallProcessorService.start(context);
        }
        return Result.success();
    }

    private static boolean isResendEnabled(@NonNull Context context) {
        return new Settings(context).getBoolean(KEY_PREF_RESEND_UNSENT, true);
    }

    public static void enable(@NonNull Context context) {
        WorkManager manager = WorkManager.getInstance();
        
        if (isResendEnabled(context)) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(ResendWorker.class, 5, TimeUnit.MINUTES)
                    .addTag(WORKER_TAG)
                    .setConstraints(constraints)
                    .build();
            manager.enqueue(request);
            log.debug("Enabled");
        } else {
            manager.cancelAllWorkByTag(WORKER_TAG);
            log.debug("Disabled");
        }
    }
}
