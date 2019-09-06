package com.bopr.android.smailer;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bopr.android.smailer.util.Util.requireNonNull;

/**
 * Service that sends mail.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallProcessorService extends JobIntentService {

    private static Logger log = LoggerFactory.getLogger("CallProcessorService");

    private static final int JOB_ID = 1;
    private static final String ACTION_EVENT = "event";
    private static final String ACTION_PENDING = "pending";
    private static final String EXTRA_EVENT = "event";

    private CallProcessor callProcessor;
    private Database database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = new Database(this);
        callProcessor = new CallProcessor(this, database, new GeoLocator(this, database));
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        log.debug("Started");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        database.close();
        super.onDestroy();

        log.debug("Destroyed");
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        log.debug("Handling intent: " + intent);

        switch (requireNonNull(intent.getAction())) {
            case ACTION_EVENT:
                PhoneEvent event = intent.getParcelableExtra(EXTRA_EVENT);
                callProcessor.process(event);
                break;
            case ACTION_PENDING:
                callProcessor.processPending();
                break;
        }
    }

    protected static Intent createIntent(Context context, PhoneEvent event) {
        Intent intent = new Intent(context, CallProcessorService.class);
        intent.setAction(ACTION_EVENT);
        intent.putExtra(EXTRA_EVENT, event);
        return intent;
    }

    protected static Intent createResendIntent(Context context) {
        Intent intent = new Intent(context, CallProcessorService.class);
        intent.setAction(ACTION_PENDING);
        return intent;
    }

    /**
     * Start service for single event
     *
     * @param context context
     * @param event   event
     */
    public static void start(Context context, PhoneEvent event) {
        enqueueWork(context, CallProcessorService.class, JOB_ID, createIntent(context, event));
    }

    /**
     * Start service for all pending events
     *
     * @param context context
     */
    public static void start(Context context) {
        enqueueWork(context, CallProcessorService.class, JOB_ID, createResendIntent(context));
    }
}