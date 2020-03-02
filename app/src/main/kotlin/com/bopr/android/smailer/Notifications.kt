package com.bopr.android.smailer

import android.app.*
import android.app.Notification.*
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.bopr.android.smailer.ui.*
import com.bopr.android.smailer.util.Mockable
import java.lang.System.currentTimeMillis

/**
 * Produces notifications.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class Notifications(private val context: Context) {

    private val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private val statusBuilder: NotificationCompat.Builder
    private val messagesBuilder: NotificationCompat.Builder
    private val errorsBuilder: NotificationCompat.Builder

    init {
        val statusChannelId = "com.bopr.android.smailer.status"
        val notificationsChannelId = "com.bopr.android.smailer.notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(statusChannelId,
                    context.getString(R.string.status), IMPORTANCE_LOW))
            manager.createNotificationChannel(NotificationChannel(notificationsChannelId,
                    context.getString(R.string.notifications), IMPORTANCE_LOW))
        }

        statusBuilder = NotificationCompat.Builder(context, statusChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(targetIntent(TARGET_MAIN))
                .setOngoing(true)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setCategory(CATEGORY_SERVICE)
                    }
                }

        messagesBuilder = NotificationCompat.Builder(context, notificationsChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setCategory(CATEGORY_MESSAGE)
                    }
                }

        errorsBuilder = NotificationCompat.Builder(context, notificationsChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentText(context.getString(R.string.tap_to_check_settings))
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setCategory(CATEGORY_ERROR)
                    }
                }
    }

    fun serviceNotification(): Notification {
        return statusBuilder
                .setWhen(currentTimeMillis())
                .setContentTitle(context.getString(R.string.service_running))
                .build()
    }

    fun showMessage(title: String, message: String? = null, @Target target: Int) {
        manager.notify("message", ++messageId,
                messagesBuilder
                        .setWhen(currentTimeMillis())
                        .setContentTitle(title)
                        .setContentText(message)
                        .setContentIntent(targetIntent(target))
                        .build())
    }

    fun showError(title: String, @Target target: Int) {
        manager.notify("error", ++errorId,
                errorsBuilder
                        .setWhen(currentTimeMillis())
                        .setContentTitle(title)
                        .setContentIntent(targetIntent(target))
                        .build())
    }

    fun showMessage(@StringRes messageRes: Int, @Target target: Int) {
        showMessage(
                title = context.getString(messageRes),
                target = target
        )
    }

    fun showError(@StringRes messageRes: Int, @Target target: Int) {
        showError(
                title = context.getString(messageRes),
                target = target
        )
    }

    fun showMailError(@StringRes reasonRes: Int, @Target target: Int) {
        showError(
                title = context.getString(R.string.unable_send_email, context.getString(reasonRes)),
                target = target
        )
    }

    fun showRemoteAction(message: String) {
        showMessage(
                title = context.getString(R.string.remote_action),
                message = message,
                target = TARGET_MAIN
        )
    }

    fun cancelAllErrors() {
        while (errorId >= 0) {
            manager.cancel("error", errorId--)
        }
    }

    private fun targetIntent(@Target target: Int): PendingIntent {
        return when (target) {
            TARGET_MAIN ->
                activityIntent(MainActivity::class.java)
            TARGET_HISTORY ->
                activityIntent(HistoryActivity::class.java)
            TARGET_RECIPIENTS ->
                activityIntent(RecipientsActivity::class.java)
            TARGET_REMOTE_CONTROL ->
                activityIntent(RemoteControlActivity::class.java)
            TARGET_RULES ->
                activityIntent(RulesActivity::class.java)
            else ->
                throw IllegalArgumentException("Invalid target")
        }
    }

    private fun activityIntent(activityClass: Class<out Activity>): PendingIntent {
        return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(Intent(context, activityClass))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)!!
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TARGET_MAIN, TARGET_RULES, TARGET_HISTORY, TARGET_RECIPIENTS, TARGET_REMOTE_CONTROL)
    annotation class Target

    companion object {

        private var messageId = -1
        private var errorId = -1

        const val SERVICE_NOTIFICATION_ID = 19158

        const val TARGET_MAIN = 0
        const val TARGET_RULES = 1
        const val TARGET_HISTORY = 2
        const val TARGET_RECIPIENTS = 3
        const val TARGET_REMOTE_CONTROL = 4
    }

}