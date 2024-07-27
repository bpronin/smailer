package com.bopr.android.smailer

import android.app.Activity
import android.app.Notification
import android.app.Notification.CATEGORY_ERROR
import android.app.Notification.CATEGORY_MESSAGE
import android.app.Notification.CATEGORY_SERVICE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.ui.EmailSettingsActivity
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.ui.RemoteControlActivity
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.isValidEmailAddressList
import java.lang.System.currentTimeMillis
import kotlin.reflect.KClass

/**
 * Produces notifications.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class NotificationsHelper(private val context: Context) {

    private val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    private val statusBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID_STATUS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(activityIntent(MainActivity::class))
            .setOngoing(true)
            .apply {
                setCategory(CATEGORY_SERVICE)
            }

    private val infoBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID_NOTIFICATIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .apply {
                setCategory(CATEGORY_MESSAGE)
            }

    private val errorsBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID_NOTIFICATIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentText(context.getString(R.string.check_settings))
            .apply {
                setCategory(CATEGORY_ERROR)
            }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_STATUS,
                    context.getString(R.string.status), IMPORTANCE_LOW
                )
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_NOTIFICATIONS,
                    context.getString(R.string.notifications), IMPORTANCE_LOW
                )
            )
        }
    }

    fun serviceNotification(): Notification {
        return statusBuilder
            .setWhen(currentTimeMillis())
            .setContentTitle(context.getString(R.string.service_running))
            .build()
    }

    fun showMailSendSuccess() {
        notifyInfo(
            context.getString(R.string.email_successfully_send),
            null,
            MainActivity::class
        )
    }

    fun showRemoteAction(message: String, target: KClass<out Activity>) {
        notifyInfo(
            context.getString(R.string.remote_action),
            message,
            target
        )
    }

    fun showRemoteAccountError() {
        notifyError(
            SERVICE_ACCOUNT_ERROR,
            context.getString(R.string.service_account_not_found),
            RemoteControlActivity::class
        )
    }

    internal fun cancelError(notificationId: Int) {
        manager.cancel(TAG_ERROR, notificationId)
    }

    internal fun onSettingsChanged(settings: Settings, key: String?) {
        when (key) {
            PREF_EMAIL_SENDER_ACCOUNT ->
                if (AccountHelper(context).isGoogleAccountExists(
                        settings.getString(
                            PREF_EMAIL_SENDER_ACCOUNT
                        )
                    )) {
                    cancelError(GOOGLE_ACCOUNT_ERROR)
                }

            PREF_REMOTE_CONTROL_ACCOUNT ->
                if (AccountHelper(context).isGoogleAccountExists(
                        settings.getString(
                            PREF_REMOTE_CONTROL_ACCOUNT
                        )
                    )) {
                    cancelError(SERVICE_ACCOUNT_ERROR)
                }

            PREF_RECIPIENTS_ADDRESS ->
                if (isValidEmailAddressList(settings.getEmailRecipients())) {
                    cancelError(RECIPIENTS_ERROR)
                }
        }
    }

    fun notifyInfo(title: String, text: String? = null, target: KClass<out Activity>) {
        manager.notify(
            TAG_MESSAGE, nextInfoNotificationId++, infoBuilder
                .setWhen(currentTimeMillis())
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(activityIntent(target))
                .build()
        )
    }

    fun notifyError(notificationId: Int, title: String, target: KClass<out Activity>) {
        manager.notify(
            TAG_ERROR, notificationId, errorsBuilder
                .setWhen(currentTimeMillis())
                .setContentTitle(title)
                .setContentIntent(activityIntent(target))
                .build()
        )
    }

    private fun activityIntent(activityClass: KClass<out Activity>): PendingIntent {
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(Intent(context, activityClass.java))
            .getPendingIntent(0, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)!!
    }

    companion object {

        private var nextInfoNotificationId = 0

        private const val CHANNEL_ID_STATUS = "com.bopr.android.smailer.status"
        private const val CHANNEL_ID_NOTIFICATIONS = "com.bopr.android.smailer.notifications"

        private const val TAG_ERROR = "error"
        private const val TAG_MESSAGE = "message"

        const val SERVICE_NOTIFICATION_ID = 19158

        const val GOOGLE_ACCOUNT_ERROR = 1000
        const val SERVICE_ACCOUNT_ERROR = 1001
        const val RECIPIENTS_ERROR = 1002
        const val GOOGLE_ACCESS_ERROR = 1003
        const val GOOGLE_MAIL_ERROR = 1004

        const val TELEGRAM_ERROR = 1005
    }

}