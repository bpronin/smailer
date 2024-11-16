package com.bopr.android.smailer.control

import com.bopr.android.smailer.external.Firebase
import com.bopr.android.smailer.external.Firebase.Companion.FCM_ACTION
import com.bopr.android.smailer.external.Firebase.Companion.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.external.Firebase.Companion.FCM_SENDER
import com.bopr.android.smailer.sync.SyncWorker.Companion.syncAppDataWithGoogleCloud
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.bopr.android.smailer.util.Logger

/**
 * Performs actions when specific firebase messages received.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class FirebaseControlService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        log.info("Received message: ${message.data}")

        Firebase(this).requestToken { token ->
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
    }
}