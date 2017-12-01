package com.bopr.android.smailer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bopr.android.smailer.util.db.XCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bopr.android.smailer.MailerService.createEventIntent;
import static com.bopr.android.smailer.Settings.*;
import static com.bopr.android.smailer.util.AndroidUtil.isServiceRunning;

/**
 * Class OutgoingSmsService.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class OutgoingSmsService extends Service {

    private static Logger log = LoggerFactory.getLogger("OutgoingSmsService");

    private ContentObserver contentObserver;
    private ContentResolverWrapper contentResolver;
    private Looper looper;
    private String lastProcessedMessage;

    public OutgoingSmsService() {
        setContentResolver(new ContentResolverWrapper(this));
    }

    protected void setContentResolver(ContentResolverWrapper wrapper) {
        this.contentResolver = wrapper;
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("OutgoingSmsService");
        thread.start();

        looper = thread.getLooper();
        Handler handler = new Handler(looper);
        contentObserver = new ContentObserver(handler) {

            @Override
            public void onChange(boolean selfChange) {
                onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                /* this method can be called multiple times so we need to remember processed message */
                if (uri != null) {
                    String id = uri.getLastPathSegment();
                    if (id != null && !id.equals(lastProcessedMessage)) {
                        lastProcessedMessage = id;
                        processSms(id);
                    }
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.debug("running");
        contentResolver.registerContentObserver(Uri.parse("content://sms"), true, contentObserver);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        looper.quit();
        contentResolver.unregisterContentObserver(contentObserver);
        super.onDestroy();
        log.debug("destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void processSms(String id) {
        log.debug("processing sms: " + id);

        new XCursor<Void>(getContentResolver().query(Uri.parse("content://sms/sent"), null,
                "_id=?", new String[]{id}, null)) {

            @Override
            public Void get() {
                startMailService(getString("address"), getLong("date"), getString("body"));
                return null;
            }
        }.getAndClose();
    }

    private void startMailService(String address, long date, String body) {
        log.debug("starting mail service");

        PhoneEvent event = new PhoneEvent();
        event.setIncoming(false);
        event.setPhone(address);
        event.setStartTime(date);
        event.setText(body);

        startService(createEventIntent(this, event));
    }

    @NonNull
    protected static Intent createServiceIntent(Context context) {
        return new Intent(context, OutgoingSmsService.class);
    }

    public static void toggle(Context context) {
        if (isServiceEnabled(context) && isTriggerEnabled(context, VAL_PREF_TRIGGER_OUT_SMS)) {
            if (!isServiceRunning(context, OutgoingSmsService.class)) {
                context.startService(createServiceIntent(context));
            }
        } else {
            if (isServiceRunning(context, OutgoingSmsService.class)) {
                context.stopService(createServiceIntent(context));
            }
        }
    }

    /**
     * This is only for testing purposes since we cannot either use nor override
     * {@link android.content.ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)} and
     * {@link android.content.ContentResolver#unregisterContentObserver(ContentObserver)}
     * in tests.
     */
    protected static class ContentResolverWrapper {

        private Context context;

        public ContentResolverWrapper(Context context) {
            this.context = context;
        }

        public void registerContentObserver(@NonNull Uri uri, boolean notifyForDescendents,
                                            @NonNull ContentObserver observer) {
            context.getContentResolver().registerContentObserver(uri, notifyForDescendents, observer);
        }

        public void unregisterContentObserver(@NonNull ContentObserver observer) {
            context.getContentResolver().unregisterContentObserver(observer);
        }

    }

}
