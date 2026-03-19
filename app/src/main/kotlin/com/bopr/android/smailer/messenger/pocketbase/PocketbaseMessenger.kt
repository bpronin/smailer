package com.bopr.android.smailer.messenger.pocketbase

import android.content.Context
import com.bopr.android.smailer.NotificationData
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_POCKETBASE
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_BASE_URL
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_PASSWORD
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_USER
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.SENT_BY_POCKETBASE
import com.bopr.android.smailer.messenger.Messenger
import com.bopr.android.smailer.messenger.pocketbase.PocketbaseException.Code.POCKETBASE_BAD_ADDRESS
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.ui.PocketbaseSettingsActivity
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.getLocalizedText

/**
 * Pocketbase messenger.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class PocketbaseMessenger(private val context: Context) : Messenger(context, SENT_BY_POCKETBASE) {

    private var initialized: Boolean = false
    private lateinit var client: PocketbaseClient
    override val isInitialized get() = initialized

    override suspend fun doInitialize() {
        initialized = false
        val settings = context.settings
        if (settings.getBoolean(PREF_POCKETBASE_MESSENGER_ENABLED)) {

            client = try {
                PocketbaseClient(settings.getString(PREF_POCKETBASE_BASE_URL, ""))
            } catch (x: Throwable) {
                throw PocketbaseException(POCKETBASE_BAD_ADDRESS, x)
            }

            client.auth(
                user = settings.getString(PREF_POCKETBASE_USER, ""),
                password = settings.getString(PREF_POCKETBASE_PASSWORD, "")
            )

            initialized = true
            log.debug("Initialized")
        }
    }

    override suspend fun doSend(event: Event) {
        log.debug("Sending")
        client.insertEvent(event)
        log.info("Sent")
    }

    override fun getSuccessNotification() = NotificationData(
        title = context.getString(R.string.message_sent_to_pocketbase),
        target = MainActivity::class
    )

    override fun getErrorNotification(error: Throwable): NotificationData? {
        when (error) {
            is PocketbaseException -> {
                return NotificationData(
                    id = NTF_POCKETBASE,
                    text = context.getString(error.getLocalizedText()),
                    target = PocketbaseSettingsActivity::class
                )
            }
            else -> {
                log.warn("Unhandled error: $error")
                return null
            }
        }
    }

    companion object {
        private val log = Logger("PocketbaseMessenger")
    }
}