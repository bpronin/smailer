package com.bopr.android.smailer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.bopr.android.smailer.settings.SettingsActivity;

/**
 * Class Notifications.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class Notifications {

    private static final int ID_MAIL_ERROR = 101;

    private Notifications() {
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private static void showAlert(Context context, String text) {
        Resources r = context.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder
                .setContentIntent(createIntent(context))
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setTicker(r.getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentTitle(r.getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .build();

        getNotificationManager(context).notify(ID_MAIL_ERROR, notification);
    }

    private static PendingIntent createIntent(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SettingsActivity.class);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void removeMailError(Context context) {
        getNotificationManager(context).cancel(ID_MAIL_ERROR);
    }

    public static void showMailError(Context context) {
        showAlert(context, context.getResources().getString(R.string.message_error_sending_email));
    }

    public static void showMailAuthenticationError(Context context) {
        showAlert(context, context.getResources().getString(R.string.message_email_authentication));
    }
}
