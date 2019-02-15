package com.bopr.android.smailer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;

import static com.bopr.android.smailer.CallProcessorService.startMailService;
import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.preferences;
import static com.bopr.android.smailer.util.AndroidUtil.hasInternetConnection;

/**
 * Watches for internet connection status to start resending unsent email.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ResendService extends Service {

    private static final Logger log = LoggerFactory.getLogger("ResendService");
    private static final long TIMER_PERIOD = TimeUnit.MINUTES.toMillis(5);

    private Handler handler;
    private Runnable task;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        task = new Runnable() {

            @Override
            public void run() {
                onTimer();
                handler.postDelayed(this, TIMER_PERIOD);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(task);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(task);
        super.onDestroy();
        log.debug("Destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onTimer() {
        log.debug("Running");
        if (isPreferenceEnabled(this) && hasInternetConnection(this)) {
            startMailService(this);
            log.debug("Started mailer service");
        }
    }

    private static boolean isPreferenceEnabled(Context context) {
        return preferences(context).getBoolean(KEY_PREF_RESEND_UNSENT, true);
    }

    /**
     * Starts or stops the service depending on preferences
     *
     * @param context context
     */
    public static void toggleService(Context context) {
        Intent intent = new Intent(context, ResendService.class);
        if (isPreferenceEnabled(context)) {
            context.startService(intent);
            log.debug("Enabled");
        } else {
            context.stopService(intent);
            log.debug("Disabled");
        }
    }
}
