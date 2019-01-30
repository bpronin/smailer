package com.bopr.android.smailer;

import android.content.Context;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.getPreferences;

/**
 * Watches for internet connection status to start resending unsent email.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ResendService extends GcmTaskService {

    private static final Logger log = LoggerFactory.getLogger("ResendService");
    private static final String TASK_TAG = "resend-task";
    private static final long PERIOD = TimeUnit.MINUTES.toSeconds(5);

    @Override
    public int onRunTask(TaskParams taskParams) {
        log.debug("Running");

        if (isPreferenceEnabled(this)) {
            startService(MailerService.createResendIntent(this));
            log.debug("Started mailer service");
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.debug("Destroyed");
    }

    private static boolean isPreferenceEnabled(Context context) {
        return getPreferences(context).getBoolean(KEY_PREF_RESEND_UNSENT, true);
    }

    /**
     * Starts or stops the service depending on preferences
     *
     * @param context context
     */
    public static void toggleService(Context context) {
        GcmNetworkManager manager = GcmNetworkManager.getInstance(context);

        if (isPreferenceEnabled(context)) {
            PeriodicTask task = new PeriodicTask.Builder()
                    .setService(ResendService.class)
                    .setPersisted(true)
                    .setTag(TASK_TAG)
                    .setPeriod(PERIOD)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED) /* default */
                    .setUpdateCurrent(true)
                    .build();

            manager.schedule(task);
            log.debug("Enabled");
        } else {
            manager.cancelTask(TASK_TAG, ResendService.class);
            log.debug("Disabled");
        }
    }
}
