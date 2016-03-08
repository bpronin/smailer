package com.bopr.android.smailer;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.bopr.android.smailer.util.LocationProvider;

/**
 * Class MailerService.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MailerService extends IntentService {

    private static final String TAG = "bopr.MailerService";

    public static final String EXTRA_SOURCE = "source";
    public static final String EXTRA_DIRECTION = "direction";
    public static final String EXTRA_MISSED = "missed";
    public static final String EXTRA_PHONE_NUMBER = "phone_number";
    public static final String EXTRA_START_TIME = "start_time";
    public static final String EXTRA_END_TIME = "end_time";
    public static final String EXTRA_SMS_TEXT = "sms_text";

    public static final int SOURCE_SMS = 0;
    public static final int SOURCE_CALL = 1;

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private LocationProvider locationProvider;

    public MailerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationProvider = new LocationProvider(this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        locationProvider.start();
        super.onStart(intent, startId);
        Log.d(TAG, "Service started");
    }

    @Override
    public void onDestroy() {
        locationProvider.stop();
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Processing mailer service intent");

        MailMessage message = new MailMessage(
                intent.getStringExtra(EXTRA_PHONE_NUMBER),
                intent.getIntExtra(EXTRA_DIRECTION, 0) == DIRECTION_INCOMING,
                intent.getLongExtra(EXTRA_START_TIME, 0),
                intent.getLongExtra(EXTRA_END_TIME, 0),
                intent.getBooleanExtra(EXTRA_MISSED, false),
                intent.getIntExtra(EXTRA_SOURCE, 0) == SOURCE_SMS,
                intent.getStringExtra(EXTRA_SMS_TEXT),
                locationProvider.getLocation()
        );

        new Mailer().send(this, message);
    }

}
