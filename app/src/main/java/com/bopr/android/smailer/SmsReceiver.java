package com.bopr.android.smailer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.*;
import static com.bopr.android.smailer.settings.Settings.*;

/**
 * SMS receiver. Receives SMS intents and starts mailer service.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "bopr.SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isEnabled(context)) {
            Log.d(TAG, "Processing SMS intent: " + intent);

            Intent serviceIntent = new Intent(context, MailerService.class);
            serviceIntent.fillIn(intent, Intent.FILL_IN_DATA);
            context.startService(serviceIntent);
        }
    }

    private boolean isEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_STORAGE_NAME, MODE_PRIVATE);
        return preferences.getBoolean(KEY_PREF_SERVICE_ENABLED, true);
    }

}
