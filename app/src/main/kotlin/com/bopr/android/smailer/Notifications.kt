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

/**
 * Produces notifications.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
class Notifications(private val context: Context) {

    private val manager: NotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    fun serviceNotification(): Notification {
        val builder = builder(context.getString(R.string.service_running), null, TARGET_MAIN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_SERVICE)
        }
        return builder.build()
    }

    fun showMessage(@StringRes messageRes: Int, @Target target: Int) {
        showMessage(context.getString(messageRes), target)
    }

    fun showError(@StringRes messageRes: Int, @Target target: Int) {
        showError(context.getString(messageRes), target)
    }

    fun showMailError(@StringRes reasonRes: Int, @Target target: Int) {
        showError(context.getString(R.string.unable_send_email, context.getString(reasonRes)), target)
    }

    fun showRemoteAction(@StringRes messageRes: Int, argument: String) {
        showMessage(context.getString(messageRes, argument), TARGET_HISTORY)
    }

    fun hideAllErrors() {
        while (errorId >= 0) {
            manager.cancel("error", errorId--)
        }
    }

    private fun getChannelId(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.getNotificationChannel(CHANNEL_ID)?.run {
                manager.createNotificationChannel(NotificationChannel(CHANNEL_ID,
                        context.getString(R.string.notifications), IMPORTANCE_LOW))
            }
        }
        return CHANNEL_ID
    }

    private fun showMessage(title: String, @Target target: Int) {
        val builder = builder(title, null, target).setAutoCancel(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_MESSAGE)
        }
        manager.notify("message", ++messageId, builder.build())
    }

    private fun showError(text: String, @Target target: Int) {
        val builder = builder(text, context.getString(R.string.tap_to_check_settings), target).setAutoCancel(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_ERROR)
        }
        manager.notify("error", ++errorId, builder.build())
    }

    private fun builder(title: String, text: String?, @Target target: Int): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, getChannelId())
                .setContentIntent(createIntent(target))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
    }

    private fun createIntent(@Target target: Int): PendingIntent? {
        return when (target) {
            TARGET_MAIN ->
                createActivityIntent(MainActivity::class.java)
            TARGET_HISTORY ->
                createActivityIntent(HistoryActivity::class.java)
            TARGET_RECIPIENTS ->
                createActivityIntent(RecipientsActivity::class.java)
            TARGET_REMOTE_CONTROL ->
                createActivityIntent(RemoteControlActivity::class.java)
            TARGET_RULES ->
                createActivityIntent(RulesActivity::class.java)
            else ->
                null
        }
    }

    private fun createActivityIntent(activityClass: Class<out Activity>): PendingIntent {
        return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(Intent(context, activityClass))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)!!
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TARGET_MAIN, TARGET_RULES, TARGET_HISTORY, TARGET_RECIPIENTS, TARGET_REMOTE_CONTROL)
    annotation class Target

    companion object {

        private const val CHANNEL_ID = "com.bopr.android.smailer"

        private var messageId = -1
        private var errorId = -1

        const val TARGET_MAIN = 0
        const val TARGET_RULES = 1
        const val TARGET_HISTORY = 2
        const val TARGET_RECIPIENTS = 3
        const val TARGET_REMOTE_CONTROL = 4
    }

}