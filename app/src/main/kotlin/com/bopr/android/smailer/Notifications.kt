package com.bopr.android.smailer

import android.app.*
import android.app.Notification.*
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
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
    //todo send to one channel?
    //todo print information in bold (title?)
    private fun getChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID,
                    context.getString(R.string.notifications), IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
        return CHANNEL_ID
    }

    internal val foregroundServiceNotification: Notification
        get() {
            val builder = createBuilder(context.getString(R.string.service_running), ACTION_SHOW_MAIN)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setCategory(CATEGORY_SERVICE)
            }
            return builder.build()
        }

    fun showMessage(@StringRes messageRes: Int, action: Int) {
        showMessage(context.getString(messageRes), action)
    }

    fun showError(@StringRes messageRes: Int, action: Int) {
        showError(context.getString(messageRes), action)
    }

    fun showMailError(@StringRes reasonRes: Int, action: Int) {
        showError(context.getString(R.string.unable_send_email, context.getString(reasonRes)), action)
    }

    fun showRemoteAction(@StringRes messageRes: Int, argument: String) {
        showError(context.getString(messageRes, argument), ACTION_SHOW_HISTORY)
    }

    fun hideAllErrors() {
        while (errorId >= 0) {
            manager.cancel("error", errorId--)
        }
    }

    private fun showMessage(text: String, action: Int) {
        val builder = createBuilder(text, action).setAutoCancel(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_MESSAGE)
        }
        manager.notify("message", ++messageId, builder.build())
    }

    private fun showError(text: String, action: Int) {
        val builder = createBuilder(text, action).setAutoCancel(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(CATEGORY_ERROR)
        }
        manager.notify("error", ++errorId, builder.build())
    }

    private fun createBuilder(text: String, action: Int): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, getChannel())
                .setContentIntent(createIntent(action))
                .setSmallIcon(R.drawable.ic_notification)
                .setTicker(context.getString(R.string.app_name))
                .setContentTitle(context.getString(R.string.app_name))
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
    }

    private fun createIntent(action: Int): PendingIntent? {
        return when (action) {
            ACTION_SHOW_MAIN ->
                createActivityIntent(MainActivity::class.java)
            ACTION_SHOW_HISTORY ->
                createActivityIntent(HistoryActivity::class.java)
            ACTION_SHOW_RECIPIENTS ->
                createActivityIntent(RecipientsActivity::class.java)
            ACTION_SHOW_REMOTE_CONTROL ->
                createActivityIntent(RemoteControlActivity::class.java)
            ACTION_SHOW_RULES ->
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

    companion object {

        private const val CHANNEL_ID = "com.bopr.android.smailer"

        private var messageId = -1
        private var errorId = -1

        const val ACTION_SHOW_MAIN = 0
        const val ACTION_SHOW_RULES = 1
        const val ACTION_SHOW_HISTORY = 2
        const val ACTION_SHOW_RECIPIENTS = 3
        const val ACTION_SHOW_REMOTE_CONTROL = 4
    }

}