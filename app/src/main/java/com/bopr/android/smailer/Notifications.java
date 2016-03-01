package com.bopr.android.smailer;

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

    public static void showMailError(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        Resources r = context.getResources();
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(r.getString(R.string.app_name))
                .setContentText(r.getString(R.string.message_error_sending_email))
                .setAutoCancel(true);

        Intent intent = new Intent(context, SettingsActivity.class);
        /* The stack builder object will contain an artificial back stack for the
         started Activity.This ensures that navigating backward from the Activity leads out of
         your application to the Home screen.*/
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        /* Adds the back stack for the Intent (but not the Intent itself)*/
        stackBuilder.addParentStack(SettingsActivity.class);
        /* Adds the Intent that starts the Activity to the top of the stack*/
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        getNotificationManager(context).notify(ID_MAIL_ERROR, builder.build());
    }

    public static void removeMailError(Context context) {
        getNotificationManager(context).cancel(ID_MAIL_ERROR);
    }

}
