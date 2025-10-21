package com.bopr.android.smailer.control.firebase

import com.bopr.android.smailer.external.Firebase.Companion.FCM_ACTION
import com.bopr.android.smailer.external.Firebase.Companion.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.external.Firebase.Companion.FCM_SENDER
import com.bopr.android.smailer.external.Firebase.Companion.firebase
import com.bopr.android.smailer.sync.SyncManager.Companion.startGoogleCloudSync
import com.bopr.android.smailer.util.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Performs actions when specific firebase messages received.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class FirebaseControlService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        log.info("Received message: ${message.data}")

        firebase.requestToken { token ->
            if (message.data[FCM_SENDER] == token) {
                log.debug("Ignored self message")
            } else if (message.data[FCM_ACTION] == FCM_REQUEST_DATA_SYNC) {
                startGoogleCloudSync()
            }
        }
    }

    override fun onNewToken(token: String) {
        log.debug("Refreshed token: $token")
        /* do nothing. we are requesting token every time */
    }

    companion object {

        private val log = Logger("FirebaseControl")
    }
}