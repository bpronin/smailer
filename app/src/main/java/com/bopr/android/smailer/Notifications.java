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

    private static final int ID_MAIL_SUCCESS = 100;
    private static final int ID_MAIL_ERROR = 101;
    private Context context;

    public Notifications(Context context) {
        this.context = context;
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void hideMailError() {
        getNotificationManager().cancel(ID_MAIL_ERROR);
    }

    public void showMailError(int messageResource, long messageId) {
        showMailError(context.getResources().getString(messageResource), messageId);
    }

    public void showMailError(String text, long messageId) {
        Resources r = context.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder
                .setContentIntent(createSettingsIntent())
                .setSmallIcon(R.drawable.alert)
                .setTicker(r.getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentTitle(r.getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .build();

        getNotificationManager().notify(ID_MAIL_ERROR, notification);
    }

    public void showMailSuccess() {
        Resources r = context.getResources();
        String text = r.getString(R.string.notification_email_send_successfully);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder
                .setSmallIcon(R.drawable.file_send)
                .setTicker(r.getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentTitle(r.getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .build();

        getNotificationManager().notify(ID_MAIL_SUCCESS, notification);
    }

    private PendingIntent createSettingsIntent() {
        Intent intent = new Intent(context, SettingsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SettingsActivity.class);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
