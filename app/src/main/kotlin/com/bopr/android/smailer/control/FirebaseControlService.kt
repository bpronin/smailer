package com.bopr.android.smailer.control

import android.content.Context
import com.bopr.android.smailer.AccountHelper.Companion.accounts
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.external.Firebase
import com.bopr.android.smailer.external.Firebase.Companion.FCM_ACTION
import com.bopr.android.smailer.external.Firebase.Companion.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.external.Firebase.Companion.FCM_SENDER
import com.bopr.android.smailer.external.Firebase.Companion.firebase
import com.bopr.android.smailer.sync.Synchronizer.Companion.syncAppDataWithGoogleCloud
import com.bopr.android.smailer.util.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Performs actions when specific firebase messages received.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class FirebaseControlService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        log.info("Received message: ${message.data}")

        firebase.requestToken { token ->
            if (message.data[FCM_SENDER] == token) {
                log.debug("Ignored self message")
            } else if (message.data[FCM_ACTION] == FCM_REQUEST_DATA_SYNC) {
                syncAppDataWithGoogleCloud()
            }
        }
    }

    override fun onNewToken(token: String) {
        log.debug("Refreshed token: $token")
        /* do nothing. we are requesting token every time */
    }

    companion object {

        private val log = Logger("FirebaseControlService")

        fun Context.startFirebaseMessaging() {
            firebase.subscribe()
            settings.registerListener { _, key ->
                if (key == PREF_MAIL_SENDER_ACCOUNT) {
                    if (accounts.isGoogleAccountExists(settings.getString(PREF_MAIL_SENDER_ACCOUNT))) {
                        firebase.apply<Firebase> {
                            unsubscribe()
                            subscribe()
                        }
                    }
                }
            }
        }
    }
}