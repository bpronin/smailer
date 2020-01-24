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

    private CallProcessor callProcessor;
    private Database database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = new Database(this);
        callProcessor = new CallProcessor(this, database, new GeoLocator(this, database));
    }

    @Override
    public void onDestroy() {
        database.close();
        super.onDestroy();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        log.debug("Handling intent: " + intent);

        callProcessor.processPending();
    }

    /**
     * Starts the service
     *
     * @param context context
     */
    public static void start(Context context) {
        log.debug("Starting service");

        enqueueWork(context, PendingCallProcessorService.class, JOB_ID,
                new Intent(context, PendingCallProcessorService.class));
    }
}