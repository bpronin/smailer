package com.bopr.android.smailer;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.PeriodicWorkRequest;
import androidx.work.PeriodicWorkRequest.Builder;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;

public class ResendWorker extends Worker {

    private static final Logger log = LoggerFactory.getLogger("ResendWorker");
    private static final String WORK_TAG = "resend";

    public ResendWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        log.debug("WORK");
        return Result.success();
    }

    private static boolean isResendEnabled(Context context) {
        return new Settings(context).getBoolean(KEY_PREF_RESEND_UNSENT, true);
    }

    public static void runResendService(Context context) {
        WorkManager manager = WorkManager.getInstance();
        if (isResendEnabled(context)) {
            PeriodicWorkRequest request = new Builder(ResendWorker.class, 10, TimeUnit.SECONDS)
                    .build();
            manager.enqueue(request);
        } else {
            manager.cancelAllWork();
        }
    }
}
