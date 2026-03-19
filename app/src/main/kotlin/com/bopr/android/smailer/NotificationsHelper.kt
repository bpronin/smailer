package com.bopr.android.smailer

import android.app.Activity
import android.app.Notification.CATEGORY_ERROR
import android.app.Notification.CATEGORY_MESSAGE
import android.app.Notification.CATEGORY_SERVICE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.Singleton
import com.bopr.android.smailer.util.isValidEmailAddressList
import java.lang.System.currentTimeMillis
import kotlin.reflect.KClass

data class NotificationData(
    val id: Int = nextId++,
    val title: String? = null,
    val text: String? = null,
    val target: KClass<out Activity>
) {
    companion object {
        var nextId = 0
    }
}

/**
 * Produces notifications.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
@Mockable
class NotificationsHelper private constructor(private val context: Context) :
    SettingsAware(context) {

    private val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    private val statusBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID_STATUS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(activityIntent(MainActivity::class))
            .setOngoing(true)
            .setCategory(CATEGORY_SERVICE)

    private val infoBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID_NOTIFICATIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setCategory(CATEGORY_MESSAGE)

    private val errorsBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID_NOTIFICATIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.check_settings))
            .setAutoCancel(true)
            .setCategory(CATEGORY_ERROR)

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

    val serviceNotification = statusBuilder
        .setWhen(currentTimeMillis())
        .setContentTitle(context.getString(R.string.service_running))
        .build()

    fun notifyInfo(notification: NotificationData) {
        log.debug("Notifying info: $notification")
        manager.notify(
            TAG_MESSAGE, notification.id, infoBuilder
                .setWhen(currentTimeMillis())
                .setContentTitle(notification.title)
                .setContentText(notification.text)
                .setContentIntent(activityIntent(notification.target))
                .build()
        )
    }

    fun notifyError(notification: NotificationData) {
        log.debug("Notifying error: $notification")
        manager.notify(
            TAG_ERROR, notification.id, errorsBuilder
                .setWhen(currentTimeMillis())
                .setContentText(notification.text)
                .setContentIntent(activityIntent(notification.target))
                .build()
        )
    }

    fun cancel(notificationId: Int) {
        log.debug("Canceling notification $notificationId")
        manager.cancel(TAG_ERROR, notificationId)
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        when (key) {
            PREF_MAIL_SENDER_ACCOUNT ->
                if (context.accounts.isGoogleAccountExists(
                        settings.getString(PREF_MAIL_SENDER_ACCOUNT)
                    )
                ) {
                    cancel(NTF_GOOGLE_ACCOUNT)
                }

            PREF_EMAIL_REMOTE_CONTROL_ACCOUNT ->
                if (context.accounts.isGoogleAccountExists(
                        settings.getString(PREF_EMAIL_REMOTE_CONTROL_ACCOUNT)
                    )
                ) {
                    cancel(NTF_SERVICE_ACCOUNT)
                }

            PREF_MAIL_MESSENGER_RECIPIENTS ->
                if (isValidEmailAddressList(settings.getMailRecipients())) {
                    cancel(NTF_MAIL_RECIPIENTS)
                }
        }
    }

    private fun activityIntent(activityClass: KClass<out Activity>): PendingIntent {
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(Intent(context, activityClass.java))
            .getPendingIntent(0, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)!!
    }

    companion object {
        private val log = Logger("Notifications")
        private const val CHANNEL_ID_STATUS = "com.bopr.android.smailer.status"
        private const val CHANNEL_ID_NOTIFICATIONS = "com.bopr.android.smailer.notifications"

        private const val TAG_ERROR = "error"
        private const val TAG_MESSAGE = "message"

        const val NTF_GOOGLE_ACCESS = 1001
        const val NTF_GOOGLE_ACCOUNT = 1002
        const val NTF_MAIL = 1003
        const val NTF_MAIL_RECIPIENTS = 1004
        const val NTF_SERVICE = 1005
        const val NTF_SERVICE_ACCOUNT = 1006
        const val NTF_TELEGRAM = 1007
        const val NTF_TELEPHONY = 1008
        const val NTF_POCKETBASE = 1009

        private val singleton = Singleton { NotificationsHelper(it) }
        val Context.notifications get() = singleton.getInstance(this)
    }

}