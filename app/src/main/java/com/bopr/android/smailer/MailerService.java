package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.bopr.android.smailer.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import androidx.annotation.NonNull;

/**
 * Service that sends mail.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailerService extends IntentService {

    private static Logger log = LoggerFactory.getLogger("MailerService");

    public static final String EXTRA_INCOMING = "incoming";
    public static final String EXTRA_MISSED = "missed";
    public static final String EXTRA_PHONE_NUMBER = "phone_number";
    public static final String EXTRA_START_TIME = "start_time";
    public static final String EXTRA_END_TIME = "end_time";
    public static final String EXTRA_TEXT = "text";

    public static final String ACTION_SINGLE = "single";
    public static final String ACTION_ALL = "all";

    private Locator locator;
    private Mailer mailer;
    private Database database;

    public MailerService() {
        super("MailerService");
    }

    protected void init(Mailer mailer, Locator locator) {
        this.mailer = mailer;
        this.locator = locator;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = new Database(this);
        init(new Mailer(this, database), new Locator(this, database));
        log.debug("Created");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        locator.start();
        super.onStart(intent, startId);
        log.debug("Running");
    }

    @Override
    public void onDestroy() {
        database.close();
        locator.stop();
        super.onDestroy();
        log.debug("Destroyed");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        log.debug("Processing mailer service intent:" + intent);

        switch (Util.requireNonNull(intent.getAction())) {
            case ACTION_SINGLE:
                processEvent(parsePhoneEventIntent(intent));
                break;
            case ACTION_ALL:
                mailer.sendAllUnsent();
                break;
        }
    }

    private void processEvent(PhoneEvent event) {
        database.putEvent(event);
        if (Settings.loadFilter(this).accept(event)) {
            log.debug("Processing phone event: " + event);

            mailer.send(event);
        } else {
            log.debug("Bypassed phone event");

            event.setState(PhoneEvent.State.IGNORED);
            database.putEvent(event);
        }
    }

    @NonNull
    private PhoneEvent parsePhoneEventIntent(Intent intent) {
        PhoneEvent event = new PhoneEvent();
        event.setIncoming(intent.getBooleanExtra(EXTRA_INCOMING, true));
        event.setMissed(intent.getBooleanExtra(EXTRA_MISSED, false));
        event.setPhone(intent.getStringExtra(EXTRA_PHONE_NUMBER));
        event.setStartTime(intent.getLongExtra(EXTRA_START_TIME, 0));
        event.setEndTime(intent.getLongExtra(EXTRA_END_TIME, 0));
        event.setText(intent.getStringExtra(EXTRA_TEXT));
        event.setLocation(locator.getLocation());

        return event;
    }

    /**
     * Start service for single event
     *
     * @param context context
     * @param event   event
     */
    public static void startMailService(Context context, PhoneEvent event) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_SINGLE);
        intent.putExtra(EXTRA_INCOMING, event.isIncoming());
        intent.putExtra(EXTRA_MISSED, event.isMissed());
        intent.putExtra(EXTRA_PHONE_NUMBER, event.getPhone());
        intent.putExtra(EXTRA_START_TIME, event.getStartTime());
        intent.putExtra(EXTRA_END_TIME, event.getEndTime());
        intent.putExtra(EXTRA_TEXT, event.getText());

        context.startService(intent);
    }

    /**
     * Start service for all unprocessed events
     *
     * @param context context
     */
    public static void startMailService(Context context) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_ALL);

        context.startService(intent);
    }
}