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

import static com.bopr.android.smailer.Settings.KEY_PREF_REMOTE_CONTROL;

/**
 * Periodically pulls email for remote commands.
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
        return new Settings(context).getBoolean(KEY_PREF_REMOTE_CONTROL, true);
    }

    public static void enable(@NonNull Context context) {
        WorkManager manager = WorkManager.getInstance();

        if (isFeatureEnabled(context)) {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(RemoteControlWorker.class, 1, TimeUnit.MINUTES)
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
