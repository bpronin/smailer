package com.bopr.android.smailer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.app.TaskStackBuilder;

import com.bopr.android.smailer.ui.HistoryActivity;
import com.bopr.android.smailer.ui.MainActivity;
import com.bopr.android.smailer.ui.RecipientsActivity;
import com.bopr.android.smailer.ui.RemoteControlActivity;
import com.bopr.android.smailer.ui.RulesActivity;
import com.bopr.android.smailer.util.TagFormatter;

import java.lang.annotation.Retention;

import static android.app.Notification.CATEGORY_ERROR;
import static android.app.Notification.CATEGORY_MESSAGE;
import static android.app.Notification.CATEGORY_SERVICE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Produces notifications.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@SuppressWarnings("WeakerAccess")
public class Notifications {

    private static final String CHANNEL_ID = "com.bopr.android.smailer";

    @IntDef({TARGET_MAIN, TARGET_RULES, TARGET_HISTORY, TARGET_RECIPIENTS, TARGET_REMOTE_CONTROL})
    @Retention(SOURCE)
    public @interface Target {
    }

    public static final int TARGET_MAIN = 0;
    public static final int TARGET_RULES = 1;
    public static final int TARGET_HISTORY = 2;
    public static final int TARGET_RECIPIENTS = 3;
    public static final int TARGET_REMOTE_CONTROL = 4;

    private static int messageId = -1;
    private static int errorId = -1;

    private Context context;
    private NotificationManager manager;
    private TagFormatter formatter;

    public Notifications(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        formatter = new TagFormatter(context);
    }

    public Notification getServiceNotification() {
        Builder builder = builder(context.getString(R.string.service_running), null, TARGET_MAIN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_SERVICE);
        }

        return builder.build();
    }

    public void showMessage(@StringRes int messageRes, @Target int target) {
        showMessage(context.getString(messageRes), target);
    }

    public void showError(@StringRes int messageRes, @Target int target) {
        showError(context.getString(messageRes), target);
    }

    public void showMailError(@StringRes int reasonRes, @Target int target) {
        showError(formatter
                        .pattern(R.string.unable_send_email)
                        .put("reason", reasonRes)
                        .format(),
                target);
    }

    public void showRemoteAction(@StringRes int messageRes, String argument) {
        showMessage(formatter
                        .pattern(messageRes)
                        .put("text", argument)
                        .format(),
                TARGET_HISTORY);
    }

    public void hideAllErrors() {
        while (errorId >= 0) {
            manager.cancel("error", errorId--);
        }
    }

    private void showMessage(String text, @Target int target) {
        Builder builder = builder(text, null, target).setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_MESSAGE);
        }

        manager.notify("message", ++messageId, builder.build());
    }

    private void showError(String text, @Target int target) {
        Builder builder = builder(text, context.getString(R.string.tap_to_check_settings), target).setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_ERROR);
        }

        manager.notify("error", ++errorId, builder.build());
    }

    private Builder builder(@NonNull String title, @Nullable String text, @Target int target) {
        return new Builder(context, getChannel())
                .setContentIntent(createIntent(target))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text);
    }

    @Nullable
    private PendingIntent createIntent(@Target int target) {
        switch (target) {
            case TARGET_MAIN:
                return createActivityIntent(MainActivity.class);
            case TARGET_HISTORY:
                return createActivityIntent(HistoryActivity.class);
            case TARGET_RECIPIENTS:
                return createActivityIntent(RecipientsActivity.class);
            case TARGET_REMOTE_CONTROL:
                return createActivityIntent(RemoteControlActivity.class);
            case TARGET_RULES:
                return createActivityIntent(RulesActivity.class);
        }
        return null;
    }

    private PendingIntent createActivityIntent(@NonNull Class<? extends Activity> activityClass) {
        Intent intent = new Intent(context, activityClass);
        return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private String getChannel() {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
                        context.getString(R.string.notifications),
                        NotificationManager.IMPORTANCE_LOW));
            }
        }
        return CHANNEL_ID;
    }

}
