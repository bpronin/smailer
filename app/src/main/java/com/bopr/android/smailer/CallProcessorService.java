package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Service that processes phone event.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class CallProcessorService extends IntentService {

    private static Logger log = LoggerFactory.getLogger("CallProcessorService");

    private static final String EXTRA_EVENT = "event";

    public CallProcessorService() {
        super("call-processor");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        log.debug("Running");

        PhoneEvent event = requireNonNull(requireNonNull(intent).getParcelableExtra(EXTRA_EVENT));
        try (Database database = new Database(this)) {
            new CallProcessor(this, database).process(event);
        }
    }

    /**
     * Start the service.
     *
     * @param context context
     * @param event   event
     */
    public static void startCallProcessingService(@NonNull Context context, @NonNull PhoneEvent event) {
        log.debug("Starting service for: " + event);

        context.startService(new Intent(context, CallProcessorService.class)
                .putExtra(EXTRA_EVENT, event));
    }
}