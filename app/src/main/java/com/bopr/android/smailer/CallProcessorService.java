package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that processes phone event.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallProcessorService extends IntentService {

    private static Logger log = LoggerFactory.getLogger("CallProcessorService");

    private static final String EXTRA_EVENT = "event";

    private CallProcessor callProcessor;
    private Database database;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public CallProcessorService() {
        super("call-processor-service");
    }

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
    protected void onHandleIntent(@Nullable Intent intent) {
        log.debug("Handling intent: " + intent);

        if (intent != null) {
            PhoneEvent event = intent.getParcelableExtra(EXTRA_EVENT);
            callProcessor.process(event);
        }
    }

    /**
     * Start the service.
     *
     * @param context context
     * @param event   event
     */
    public static void start(Context context, PhoneEvent event) {
        log.debug("Starting service for: " + event);

        context.startService(new Intent(context, CallProcessorService.class)
                .putExtra(EXTRA_EVENT, event));
    }
}