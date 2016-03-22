package com.bopr.android.smailer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.bopr.android.smailer.ui.SettingsActivity;

/**
 * Creates and shows specific notifications.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Notifications {

    private static final int ID_MAIL_ERROR = 101;

    public Notifications() {
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void removeMailError(Context context) {
        getNotificationManager(context).cancel(ID_MAIL_ERROR);
    }

    public void showMailError(Context context, int messageResource) {
        showMailError(context, context.getResources().getString(messageResource));
    }

    public void showMailError(Context context, String text) {
        Resources r = context.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder
                .setContentIntent(createMailIntent(context))
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setTicker(r.getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentTitle(r.getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .build();

        getNotificationManager(context).notify(ID_MAIL_ERROR, notification);
    }

    private PendingIntent createMailIntent(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SettingsActivity.class);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
