package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.getPreferences;
import static com.bopr.android.smailer.Settings.isServiceEnabled;
import static com.bopr.android.smailer.util.AndroidUtil.hasInternetConnection;

/**
 * Starts mail service when internet connection status changes.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    private static Logger log = LoggerFactory.getLogger("ConnectivityReceiver");

    @Override
    public void onReceive(Context context, Intent intent) {
        log.debug("Received intent: " + intent);

        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
                && isServiceEnabled(context)
                && getPreferences(context).getBoolean(KEY_PREF_RESEND_UNSENT, true)
                && hasInternetConnection(context)) {
            context.startService(MailerService.createResendIntent(context));
        }
    }

}