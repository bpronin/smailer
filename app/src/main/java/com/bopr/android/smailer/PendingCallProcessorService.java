package com.bopr.android.smailer;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that processes pending phone events.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class PendingCallProcessorService extends JobIntentService {

    private static Logger log = LoggerFactory.getLogger("PendingCallProcessorService");

    private static final int JOB_ID = 1000;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        log.debug("Handling intent: " + intent);

        try (Database database = new Database(this)) {
            new CallProcessor(this, database).processPending();
        }
    }

    /**
     * Starts the service
     *
     * @param context context
     */
    public static void startPendingCallProcessorService(Context context) {
        log.debug("Starting service");

        enqueueWork(context, PendingCallProcessorService.class, JOB_ID,
                new Intent(context, PendingCallProcessorService.class));
    }
}