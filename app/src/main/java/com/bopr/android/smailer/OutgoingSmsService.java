package com.bopr.android.smailer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.bopr.android.smailer.util.db.XCursor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import androidx.annotation.Nullable;

import static com.bopr.android.smailer.CallProcessorService.startMailService;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;

/**
 * Class OutgoingSmsService.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class OutgoingSmsService extends Service {

    public static final Uri CONTENT_SMS_SENT = Uri.parse("content://sms/sent");
    public static final Uri CONTENT_SMS = Uri.parse("content://sms");
    private static Logger log = LoggerFactory.getLogger("OutgoingSmsService");

    private ContentObserver contentObserver;
    private Looper looper;
    private Notifications notifications;

    @Override
    public void onCreate() {
        notifications = new Notifications(this);

        HandlerThread thread = new HandlerThread("OutgoingSmsService");
        thread.start();

        looper = thread.getLooper();
        Handler handler = new Handler(looper);
        contentObserver = new SmsContentObserver(handler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.debug("Running");
        getContentResolver().registerContentObserver(CONTENT_SMS, true, contentObserver);
        startForeground(1, notifications.getForegroundServiceNotification());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        looper.quit();
        getContentResolver().unregisterContentObserver(contentObserver);
        super.onDestroy();
        log.debug("Destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void processEvent(String id) {
        log.debug("Processing sms: " + id);

        new XCursor<Void>(getContentResolver().query(CONTENT_SMS_SENT, null, "_id=?",
                new String[]{id}, null)) {

            @Override
            public Void mapRow() {
                long date = getLong("date");
                log.debug("Starting mail service");

                PhoneEvent event = new PhoneEvent();
                event.setIncoming(false);
                event.setPhone(getString("address"));
                event.setStartTime(date);
                event.setEndTime(date);
                event.setText(getString("body"));

                startMailService(OutgoingSmsService.this, event);
                return null;
            }
        }.findFirst();
    }


    /**
     * Starts or stops the service depending on settings
     *
     * @param context context
     */
    public static void toggleService(Context context) {
        Intent intent = new Intent(context, OutgoingSmsService.class);
        PhoneEventFilter filter = new Settings(context).getFilter();
        if (filter.getTriggers().contains(VAL_PREF_TRIGGER_OUT_SMS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
            log.debug("Enabled");
        } else {
            context.stopService(intent);
            log.debug("Disabled");
        }
    }

    private class SmsContentObserver extends ContentObserver {

        private String lastProcessed;

        private SmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            /* this method can be called multiple times so we need to remember processed message */
            if (uri != null) {
                String id = uri.getLastPathSegment();
                if (id != null && !id.equals(lastProcessed)) {
                    lastProcessed = id;
                    processEvent(id);
                }
            }
        }
    }
}
