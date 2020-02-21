package com.bopr.android.smailer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.bopr.android.smailer.util.db.RowSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

import static com.bopr.android.smailer.CallProcessorService.startCallProcessingService;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static com.bopr.android.smailer.Settings.settings;
import static com.bopr.android.smailer.util.AndroidUtil.deviceName;

/**
 * Listens to changes in sms content. Used to process outgoing SMS.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ContentObserverService extends Service {

    private static Logger log = LoggerFactory.getLogger("ContentObserverService");

    private static final Uri CONTENT_SMS_SENT = Uri.parse("content://sms/sent");
    private static final Uri CONTENT_SMS = Uri.parse("content://sms");

    private ContentObserver contentObserver;
    private Notifications notifications;
    private HandlerThread thread;

    @Override
    public void onCreate() {
        notifications = new Notifications(this);

        thread = new HandlerThread("ContentObserverService");
        thread.start();
        contentObserver = new SmsContentObserver(new Handler(thread.getLooper()));
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
        thread.quit();
        getContentResolver().unregisterContentObserver(contentObserver);
        super.onDestroy();

        log.debug("Destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void processOutgoingSms(String id) {
        log.debug("Processing outgoing sms: " + id);

        Cursor query = getContentResolver().query(CONTENT_SMS_SENT, null, "_id=?", new String[]{id}, null);
        PhoneEvent event = new SentSmsRowSet(query).findFirst();
        if (event != null) {
            startCallProcessingService(this, event);
        }
    }

    /**
     * Starts or stops the service depending on settings
     *
     * @param context context
     */
    public static void enableContentObserverService(Context context) {
        Intent intent = new Intent(context, ContentObserverService.class);
        Set<String> triggers = settings(context).getFilter().getTriggers();
        if (triggers.contains(VAL_PREF_TRIGGER_OUT_SMS)) {
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

    private static class SentSmsRowSet extends RowSet<PhoneEvent> {

        private SentSmsRowSet(Cursor query) {
            super(query);
        }

        @Override
        protected PhoneEvent get() {
            long date = getLong("date");

            PhoneEvent event = new PhoneEvent();
            event.setIncoming(false);
            event.setRecipient(deviceName());
            event.setPhone(getString("address"));
            event.setStartTime(date);
            event.setEndTime(date);
            event.setText(getString("body"));

            return event;
        }
    }

    private class SmsContentObserver extends ContentObserver {

        private Uri lastProcessed;

        private SmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            /* this method may be called multiple times so we need to remember processed uri */
            if (uri != null && !uri.equals(lastProcessed)) {
                log.debug("Processing uri: " + uri);

                List<String> segments = uri.getPathSegments();
                if (!segments.isEmpty()) {
                    String segment = segments.get(0);
                    switch (segment) {
                        case "raw":
                            log.debug("sms/raw changed");
                            break;
                        case "inbox":
                            log.debug("sms/inbox segment changed");
                            break;
                        case "sent":
                            processOutgoingSms(segments.get(1));
                            break;
                        default:
                            processOutgoingSms(segment);
                            break;
                    }
                }

                lastProcessed = uri;
            }
        }
    }
}
