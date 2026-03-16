package com.bopr.android.smailer.messenger.pocketbase

import android.content.Context
import com.bopr.android.smailer.Settings.Companion.PREF_PB_BASE_URL
import com.bopr.android.smailer.Settings.Companion.PREF_PB_PASSWORD
import com.bopr.android.smailer.Settings.Companion.PREF_PB_USER
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Messenger
import com.bopr.android.smailer.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PocketbaseMessenger(private val context: Context, private val scope: CoroutineScope) :
    Messenger {

    private var client: PocketbaseClient? = null

    override suspend fun prepare(): Boolean {
        with(context.settings) {
            if (getBoolean(PREF_TELEGRAM_MESSENGER_ENABLED)) {
                client = PocketbaseClient(getString(PREF_PB_BASE_URL, ""))
                scope.launch {
                    client?.auth(getString(PREF_PB_USER, ""), getString(PREF_PB_PASSWORD, ""))
                }
                log.debug("Prepared")
                return true
            }
            return false
        }
    }

    override suspend fun send(
        event: Event,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        client?.apply {
            try {
                scope.launch {
                    insertIntoEvents(event)
                }
                onSuccess()
            } catch (x: RuntimeException) {
                onError(x)
            }
        } ?: onError(Exception("Not prepared"))
    }

    companion object {

        private val log = Logger("RestMessenger")
    }
}