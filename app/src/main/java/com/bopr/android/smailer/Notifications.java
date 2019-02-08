package com.bopr.android.smailer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.bopr.android.smailer.ui.MainActivity;

/**
 * Produces notifications.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Notifications {

    private static final int ID_MAIL_SUCCESS = 100;
    private static final int ID_MAIL_ERROR = 101;
    private static final String EXTRA_MESSAGE_ID = "message_id";

    public static final int ACTION_SHOW_MAIN = 0;
    public static final int ACTION_SHOW_SERVER = 1;
    public static final int ACTION_SHOW_RECIPIENTS = 2;
    public static final int ACTION_SHOW_CONNECTION = 3;
    public static final int ACTION_SHOW_LOG = 4;

    private Context context;

    public Notifications(Context context) {
        this.context = context;
    }

    private NotificationManager notificationManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void hideMailError() {
        notificationManager().cancel(ID_MAIL_ERROR);
    }

    public void showMailError(int messageResource, long messageId, int action) {
        showMailError(context.getResources().getString(R.string.notification_error_mail_general) + " " +
                context.getResources().getString(messageResource), messageId, action);
    }

    public void showMailError(String text, long messageId, int action) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        Notification notification = builder
                .setContentIntent(createIntent(action, messageId))
                .setSmallIcon(R.drawable.ic_alert)
                .setTicker(context.getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .build();

        notificationManager().notify(ID_MAIL_ERROR, notification);
    }

    public void showMailSuccess(long messageId) {
        String text = context.getResources().getString(R.string.notification_email_send);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        Notification notification = builder
                .setContentIntent(createIntent(ACTION_SHOW_LOG, messageId))
                .setSmallIcon(R.drawable.ic_file_send)
                .setTicker(context.getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .build();

        notificationManager().notify(ID_MAIL_SUCCESS, notification);
    }

    private PendingIntent createIntent(int action, long messageId) {
        switch (action) {
            case ACTION_SHOW_RECIPIENTS:
            case ACTION_SHOW_SERVER:
            case ACTION_SHOW_MAIN:
            case ACTION_SHOW_LOG:
                return createActivityIntent(MainActivity.class, messageId);
            case ACTION_SHOW_CONNECTION:
                Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                return PendingIntent.getActivities(context, 0, new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return null;
    }

    private PendingIntent createActivityIntent(Class activityClass, long messageId) {
        Intent intent = new Intent(context, activityClass);
        intent.putExtra(EXTRA_MESSAGE_ID, messageId);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
