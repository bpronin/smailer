package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import static com.bopr.android.smailer.util.AndroidUtil.*;

/**
 * Starts mail service for when internet connection status changes.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    private static final String TAG = "ConnectivityReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent: " + intent);

        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) && hasInternetConnection(context)) {
            Log.d(TAG, "Resending unsent messages");
            MailerService.startForResendUnsent(context);
        }
    }

}