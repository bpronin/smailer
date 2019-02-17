package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bopr.android.smailer.util.Util.requireNonNull;

/**
 * Service that sends mail.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallProcessorService extends IntentService {

    private static Logger log = LoggerFactory.getLogger("CallProcessorService");

    private static final String ACTION_SINGLE = "single";
    private static final String ACTION_ALL = "all";
    private static final String EXTRA_EVENT = "event";

    private CallProcessor callProcessor;
    private Database database;

    public CallProcessorService() {
        super("CallProcessorService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = new Database(this);
        callProcessor = new CallProcessor(this, database, new GeoLocator(this, database));
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        log.debug("Running");
    }

    @Override
    public void onDestroy() {
        database.close();
        super.onDestroy();

        log.debug("Destroyed");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        log.debug("Handling intent: " + intent);

        switch (requireNonNull(intent.getAction())) {
            case ACTION_SINGLE:
                PhoneEvent event = intent.getParcelableExtra(EXTRA_EVENT);
                callProcessor.process(event);
                break;
            case ACTION_ALL:
                callProcessor.processAll();
                break;
        }
    }

    /**
     * Start service for single event
     *
     * @param context context
     * @param event   event
     */
    public static void start(Context context, PhoneEvent event) {
        Intent intent = new Intent(context, CallProcessorService.class);
        intent.setAction(ACTION_SINGLE);
        intent.putExtra(EXTRA_EVENT, event);

        context.startService(intent);
    }

    /**
     * Start service for all unprocessed events
     *
     * @param context context
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, CallProcessorService.class);
        intent.setAction(ACTION_ALL);

        context.startService(intent);
    }
}