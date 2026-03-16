package com.bopr.android.smailer.messenger.pocketbase

import android.content.Context
import com.bopr.android.smailer.NotificationData
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_BASE_URL
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_PASSWORD
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_USER
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.SENT_BY_POCKETBASE
import com.bopr.android.smailer.messenger.Messenger
import com.bopr.android.smailer.util.Logger

/**
 * Pocketbase messenger.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class PocketbaseMessenger(private val context: Context) : Messenger(context, SENT_BY_POCKETBASE) {

    private var client: PocketbaseClient? = null

    override suspend fun prepare(): Boolean {
        val settings = context.settings
        if (settings.getBoolean(PREF_POCKETBASE_MESSENGER_ENABLED)) {
            client = PocketbaseClient(settings.getString(PREF_POCKETBASE_BASE_URL, ""))
            client?.apply {
                try {
                    auth(
                        settings.getString(PREF_POCKETBASE_USER, ""),
                        settings.getString(PREF_POCKETBASE_PASSWORD, "")
                    )
                    log.debug("Client created")
                    return true
                } catch (x: Exception) {
                    log.error("Create client failed: $x")
                }
            }
        }
        return false
    }

    override suspend fun doSend(
        event: Event,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        log.debug("Sending")
        client?.apply {
            try {
                insertIntoEvents(event)
                onSuccess()
            } catch (x: Exception) {
                onError(x)
            }
        }
    }

    override fun getSuccessNotification(): NotificationData {
        TODO("Not yet implemented")
    }

    override fun getErrorNotification(error: Throwable): NotificationData {
        TODO("Not yet implemented")
    }

    companion object {

        private val log = Logger("PocketbaseMessenger")
    }
}