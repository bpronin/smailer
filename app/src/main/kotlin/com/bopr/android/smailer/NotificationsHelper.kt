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
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.util.Mockable
import com.bopr.android.smailer.util.SingletonHolder
import com.bopr.android.smailer.util.isValidEmailAddressList
import java.lang.System.currentTimeMillis
import kotlin.reflect.KClass

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

    fun createServiceNotification(): Notification {
        return statusBuilder
            .setWhen(currentTimeMillis())
            .setContentTitle(context.getString(R.string.service_running))
            .build()
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

    fun notifyError(notificationId: Int, text: String, target: KClass<out Activity>) {
        manager.notify(
            TAG_ERROR, notificationId, errorsBuilder
                .setWhen(currentTimeMillis())
                .setContentText(text)
                .setContentIntent(activityIntent(target))
                .build()
        )
    }

    internal fun cancelError(notificationId: Int) {
        manager.cancel(TAG_ERROR, notificationId)
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        when (key) {
            PREF_MAIL_SENDER_ACCOUNT ->
                if (context.accounts.isGoogleAccountExists(
                        settings.getString(PREF_MAIL_SENDER_ACCOUNT)
                    )
                ) {
                    cancelError(NTF_GOOGLE_ACCOUNT)
                }

            PREF_REMOTE_CONTROL_ACCOUNT ->
                if (context.accounts.isGoogleAccountExists(
                        settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
                    )
                ) {
                    cancelError(NTF_SERVICE_ACCOUNT)
                }

            PREF_MAIL_MESSENGER_RECIPIENTS ->
                if (isValidEmailAddressList(settings.getMailRecipients())) {
                    cancelError(NTF_MAIL_RECIPIENTS)
                }
        }
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

        const val NTF_GOOGLE_ACCESS = 1001
        const val NTF_GOOGLE_ACCOUNT = 1002
        const val NTF_MAIL = 1003
        const val NTF_MAIL_RECIPIENTS = 1004
        const val NTF_SERVICE = 1005
        const val NTF_SERVICE_ACCOUNT = 1006
        const val NTF_TELEGRAM = 1007
        const val NTF_TELEPHONY = 1008

        private val singletonHolder = SingletonHolder { NotificationsHelper(it) }
        val Context.notifications get() = singletonHolder.getInstance(this)
        val Fragment.notifications get() = requireContext().notifications
    }

}