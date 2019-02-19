package com.bopr.android.smailer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.bopr.android.smailer.ui.MainActivity;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

/**
 * Produces notifications.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class Notifications {

    private static final String CHANNEL_ID = "com.bopr.android.smailer";
    private static final String EXTRA_MESSAGE_ID = "message_id";

    private static final int ID_MAIL_SUCCESS = 100;
    private static final int ID_MAIL_ERROR = 101;
    private static final int ID_REMOTE_ACTION = 102;

    public static final int ACTION_SHOW_MAIN = 0;
    public static final int ACTION_SHOW_SERVER = 1;
    public static final int ACTION_SHOW_RECIPIENTS = 2;
    public static final int ACTION_SHOW_CONNECTION = 3;
    public static final int ACTION_SHOW_LOG = 4;

    private Context context;

    public Notifications(Context context) {
        this.context = context;
    }

    public Notification getForegroundServiceNotification() {
        String text = context.getResources().getString(R.string.service_running);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getChannel())
                .setContentIntent(createIntent(ACTION_SHOW_MAIN, null))
                .setSmallIcon(R.drawable.ic_service)
                .setTicker(context.getResources().getString(R.string.app_name))
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE);
        }

        return builder.build();
    }

    public void hideMailError() {
        getManager().cancel(ID_MAIL_ERROR);
    }

    public void showMailError(int messageResource, @Nullable Long messageId, int action) {
        showMailError(context.getResources().getString(R.string.unable_send_email) + " " +
                context.getResources().getString(messageResource), messageId, action);
    }

    public void showMailError(String text, @Nullable Long messageId, int action) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getChannel())
                .setContentIntent(createIntent(action, messageId))
                .setSmallIcon(R.drawable.ic_alert)
                .setTicker(context.getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_ERROR);
        }

        getManager().notify(ID_MAIL_ERROR, builder.build());
    }

    public void showMailSuccess(long messageId) {
        String text = context.getResources().getString(R.string.email_send);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getChannel())
                .setContentIntent(createIntent(ACTION_SHOW_LOG, messageId))
                .setSmallIcon(R.drawable.ic_file_send)
                .setTicker(context.getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE);
        }

        getManager().notify(ID_MAIL_SUCCESS, builder.build());
    }

    public void showRemoteAction(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, getChannel())
                .setContentIntent(createIntent(ACTION_SHOW_MAIN, null))
                .setSmallIcon(R.drawable.ic_service)
                .setTicker(context.getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentText(message);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE);
        }

        getManager().notify(ID_REMOTE_ACTION, builder.build());
    }

    private PendingIntent createIntent(int action, @Nullable Long messageId) {
        switch (action) {
            case ACTION_SHOW_RECIPIENTS:
//                return createActivityIntent(RecipientsActivity.class, messageId);
            case ACTION_SHOW_SERVER:
//                return createActivityIntent(ServerActivity.class, messageId);
            case ACTION_SHOW_LOG:
//                return createActivityIntent(HistoryActivity.class, messageId);
            case ACTION_SHOW_MAIN:
                return createActivityIntent(MainActivity.class, messageId);
            case ACTION_SHOW_CONNECTION:
                Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                return PendingIntent.getActivities(context, 0, new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return null;
    }

    private PendingIntent createActivityIntent(Class activityClass, @Nullable Long messageId) {
        Intent intent = new Intent(context, activityClass);
        if (messageId != null) {
            intent.putExtra(EXTRA_MESSAGE_ID, messageId);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private NotificationManager getManager() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private String getChannel() {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String name = context.getResources().getString(R.string.notifications);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            getManager().createNotificationChannel(channel);
        }
        return CHANNEL_ID;
    }
}
