package com.bopr.android.smailer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.bopr.android.smailer.ui.HistoryActivity;
import com.bopr.android.smailer.ui.MainActivity;
import com.bopr.android.smailer.ui.OptionsActivity;
import com.bopr.android.smailer.ui.RecipientsActivity;
import com.bopr.android.smailer.ui.RulesActivity;
import com.bopr.android.smailer.util.TagFormatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.TaskStackBuilder;

import static android.app.Notification.CATEGORY_ERROR;
import static android.app.Notification.CATEGORY_MESSAGE;
import static android.app.Notification.CATEGORY_SERVICE;

/**
 * Produces notifications.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class Notifications {

    private static final String CHANNEL_ID = "com.bopr.android.smailer";

    public static final int ACTION_SHOW_MAIN = 0;
    public static final int ACTION_SHOW_CONNECTION_OPTIONS = 1;
    public static final int ACTION_SHOW_RULES = 2;
    public static final int ACTION_SHOW_HISTORY = 3;
    public static final int ACTION_SHOW_RECIPIENTS = 4;
    public static final int ACTION_SHOW_OPTIONS = 5;

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

    public void showMailError(int messageRes, int action) {
        String text = formatter
                .pattern(R.string.unable_send_email)
                .put("reason", messageRes)
                .format();

        showError(text, action);
    }

    public void showRemoteError(int messageRes) {
        showError(context.getString(messageRes), ACTION_SHOW_OPTIONS);
    }

    public void hideLastError() {
        manager.cancel("error", errorMessageId--);
    }

    public void showMailSuccess() {
        String text = context.getString(R.string.email_send);

        Builder builder = createBuilder(text, ACTION_SHOW_MAIN).setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_MESSAGE);
        }

        manager.notify("success", ++messageId, builder.build());
    }

    public void showRemoteAction(int messageRes, String argument) {
        String text = formatter
                .pattern(messageRes)
                .put("text", argument)
                .format();

        Builder builder = createBuilder(text, ACTION_SHOW_HISTORY).setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_MESSAGE);
        }

        manager.notify("remote", ++messageId, builder.build());
    }

    public Notification getForegroundServiceNotification() {
        Builder builder = createBuilder(context.getString(R.string.service_running), ACTION_SHOW_MAIN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_SERVICE);
        }

        return builder.build();
    }

    private void showError(String text, int action) {
        Builder builder = createBuilder(text, action).setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_ERROR);
        }

        manager.notify("error", ++errorMessageId, builder.build());
    }

    private Builder createBuilder(String text, int action) {
        return new Builder(context, getChannel())
                .setContentIntent(createIntent(action))
                .setSmallIcon(R.drawable.ic_light_service)
                .setTicker(context.getString(R.string.app_name))
                .setContentTitle(context.getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text);
    }

    @Nullable
    private PendingIntent createIntent(int action) {
        switch (action) {
            case ACTION_SHOW_MAIN:
                return createActivityIntent(MainActivity.class);
            case ACTION_SHOW_HISTORY:
                return createActivityIntent(HistoryActivity.class);
            case ACTION_SHOW_RECIPIENTS:
                return createActivityIntent(RecipientsActivity.class);
            case ACTION_SHOW_OPTIONS:
                return createActivityIntent(OptionsActivity.class);
            case ACTION_SHOW_RULES:
                return createActivityIntent(RulesActivity.class);
            case ACTION_SHOW_CONNECTION_OPTIONS:
                Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                return PendingIntent.getActivities(context, 0, new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return null;
    }

    private PendingIntent createActivityIntent(@NonNull Class<? extends Activity> activityClass) {
        Intent intent = new Intent(context, activityClass);
        return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
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
