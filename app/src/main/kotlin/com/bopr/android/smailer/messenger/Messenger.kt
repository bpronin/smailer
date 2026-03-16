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
    private val processedFlag: Bits,
) {

    protected abstract suspend fun doSend(
        event: Event,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    )

    protected abstract fun getSuccessNotification(): NotificationData

    protected abstract fun getErrorNotification(error: Throwable): NotificationData

    abstract suspend fun prepare(): Boolean

    suspend fun send(
        event: Event,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (processedFlag in event.processFlags) return

        doSend(
            event,
            onSuccess = {
                event.processFlags += processedFlag
                if (context.settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS)) {
                    context.notifications.notifyError(getSuccessNotification())
                }
                onSuccess()
            },
            onError = {
                event.processFlags -= processedFlag
                context.notifications.notifyError(getErrorNotification(it))
                onError(it)
            })
    }

}
