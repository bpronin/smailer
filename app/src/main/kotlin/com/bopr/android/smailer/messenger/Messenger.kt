package com.bopr.android.smailer.messenger

import android.content.Context
import com.bopr.android.smailer.NotificationData
import com.bopr.android.smailer.NotificationsHelper.Companion.notifications
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.util.Bits


/**
 * Sends informative messages to user.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
abstract class Messenger(
    private val context: Context,
    private val processFlag: Bits,
) {

    protected abstract val isInitialized: Boolean

    protected abstract suspend fun doInitialize()

    protected abstract suspend fun doSend(event: Event)

    protected abstract fun getSuccessNotification(): NotificationData

    protected abstract fun getErrorNotification(error: Throwable): NotificationData?

    suspend fun initialize(): Boolean {
        try {
            doInitialize()
        } catch (x: Throwable) {
            notifyError(x)
        }
        return isInitialized
    }

    suspend fun send(event: Event) {
        if (!isInitialized || processFlag in event.processFlags) return

        try {
            doSend(event)
        } catch (x: Throwable) {
            event.processFlags -= processFlag
            notifyError(x)
            return
        }

        event.processFlags += processFlag
        if (context.settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS)) {
            context.notifications.notifyInfo(getSuccessNotification())
        }
    }

    private fun notifyError(error: Throwable){
        getErrorNotification(error)?.let {
            context.notifications.notifyError(it)
        }
    }

}
