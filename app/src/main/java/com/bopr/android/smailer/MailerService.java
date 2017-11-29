package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static final String ACTION_SMS = "sms";
    public static final String ACTION_CALL = "call";
    public static final String ACTION_RESEND = "resend";

    private Locator locator;
    private Mailer mailer;

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
        Database database = new Database(this);
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
        locator.stop();
        super.onDestroy();
        log.debug("Destroyed");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        log.debug("Processing mailer service intent:" + intent);

        switch (intent.getAction()) {
            case ACTION_SMS:
                mailer.send(parseSmsIntent(intent));
                break;
            case ACTION_CALL:
                mailer.send(parseCallIntent(intent));
                break;
            case ACTION_RESEND:
                mailer.sendAllUnsent();
                break;
        }
    }

    @NonNull
    private MailMessage parseCallIntent(Intent intent) {
        MailMessage message = new MailMessage();
        message.setSms(false);
        message.setPhone(intent.getStringExtra(EXTRA_PHONE_NUMBER));
        message.setIncoming(intent.getBooleanExtra(EXTRA_INCOMING, true));
        message.setMissed(intent.getBooleanExtra(EXTRA_MISSED, false));
        message.setLocation(locator.getLocation());
        message.setStartTime(intent.getLongExtra(EXTRA_START_TIME, 0));
        if (intent.hasExtra(EXTRA_END_TIME)) {
            message.setEndTime(intent.getLongExtra(EXTRA_END_TIME, 0));
        }

        return message;
    }

    @NonNull
    private MailMessage parseSmsIntent(Intent intent) {
        MailMessage message = new MailMessage();
        message.setSms(true);
        message.setPhone(intent.getStringExtra(EXTRA_PHONE_NUMBER));
        message.setIncoming(intent.getBooleanExtra(EXTRA_INCOMING, true));
        message.setLocation(locator.getLocation());
        message.setStartTime(intent.getLongExtra(EXTRA_START_TIME, 0));
        message.setText(intent.getStringExtra(EXTRA_TEXT));
        return message;
    }

    @NonNull
    public static Intent createSmsIntent(Context context, String number, long time, String text, boolean incoming) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_SMS);
        intent.putExtra(EXTRA_PHONE_NUMBER, number);
        intent.putExtra(EXTRA_INCOMING, incoming);
        intent.putExtra(EXTRA_START_TIME, time);
        intent.putExtra(EXTRA_TEXT, text);
        return intent;
    }

    @NonNull
    public static Intent createMissedCallIntent(Context context, String number, long start) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_CALL);
        intent.putExtra(EXTRA_MISSED, true);
        intent.putExtra(EXTRA_PHONE_NUMBER, number);
        intent.putExtra(EXTRA_START_TIME, start);
        return intent;
    }

    @NonNull
    public static Intent createIncomingCallIntent(Context context, String number, long start, long end) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_CALL);
        intent.putExtra(EXTRA_PHONE_NUMBER, number);
        intent.putExtra(EXTRA_INCOMING, true);
        intent.putExtra(EXTRA_START_TIME, start);
        intent.putExtra(EXTRA_END_TIME, end);
        return intent;
    }

    @NonNull
    public static Intent createOutgoingCallIntent(Context context, String number, long start, long end) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_CALL);
        intent.putExtra(EXTRA_PHONE_NUMBER, number);
        intent.putExtra(EXTRA_INCOMING, false);
        intent.putExtra(EXTRA_START_TIME, start);
        intent.putExtra(EXTRA_END_TIME, end);
        return intent;
    }

    @NonNull
    public static Intent createResendIntent(Context context) {
        Intent intent = new Intent(context, MailerService.class);
        intent.setAction(ACTION_RESEND);
        return intent;
    }
}