package com.bopr.android.smailer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.bopr.android.smailer.ui.MainActivity;
import com.bopr.android.smailer.util.TagFormatter;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
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

    public static final int ACTION_SHOW_APP = 0;
    public static final int ACTION_SHOW_CONNECTION_OPTIONS = 1;

    private static int messageId = -1;
    private static int errorMessageId = -1;

    private Context context;
    private NotificationManager manager;
    private TagFormatter formatter;

    public Notifications(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        formatter = new TagFormatter(context);
    }

    public void showMailError(int messageRes, int action, @Nullable Long eventId) {
        String text = formatter
                .pattern(R.string.unable_send_email)
                .put("reason", messageRes)
                .format();

        showError(text, action, eventId);
    }

    public void showRemoteError(int messageRes) {
        showError(context.getString(messageRes), ACTION_SHOW_APP, null);
    }

    public void hideLastError() {
        manager.cancel("error", errorMessageId--);
    }

    public void showMailSuccess(long eventId) {
        String text = context.getString(R.string.email_send);

        Builder builder = createBuilder(text, ACTION_SHOW_APP, eventId).setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE);
        }

        manager.notify("success", ++messageId, builder.build());
    }

    public void showRemoteAction(int messageRes, String argument) {
        String text = formatter
                .pattern(messageRes)
                .put("text", argument)
                .format();

        Builder builder = createBuilder(text, ACTION_SHOW_APP, null).setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE);
        }

        manager.notify("remote", ++messageId, builder.build());
    }

    public Notification getForegroundServiceNotification() {
        Builder builder = createBuilder(context.getString(R.string.service_running), ACTION_SHOW_APP, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE);
        }

        return builder.build();
    }

    private void showError(String text, int action, Long eventId) {
        Builder builder = createBuilder(text, action, eventId).setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_ERROR);
        }

        manager.notify("error", ++errorMessageId, builder.build());

    }

    private Builder createBuilder(String text, int action, @Nullable Long eventId) {
        return new Builder(context, getChannel())
                .setContentIntent(createIntent(action, eventId))
                .setSmallIcon(R.drawable.ic_light_service)
                .setTicker(context.getString(R.string.app_name))
                .setContentTitle(context.getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text);
    }

    private PendingIntent createIntent(int action, @Nullable Long eventId) {
        switch (action) {
            case ACTION_SHOW_APP:
                return createActivityIntent(MainActivity.class, eventId);
            case ACTION_SHOW_CONNECTION_OPTIONS:
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

        return TaskStackBuilder.create(context)
                .addParentStack(activityClass)
                .addNextIntent(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private String getChannel() {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String name = context.getString(R.string.notifications);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }
        return CHANNEL_ID;
    }

}
