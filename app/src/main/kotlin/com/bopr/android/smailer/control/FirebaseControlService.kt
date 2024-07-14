package com.bopr.android.smailer.control

import com.bopr.android.smailer.external.Firebase
import com.bopr.android.smailer.external.Firebase.Companion.FCM_ACTION
import com.bopr.android.smailer.external.Firebase.Companion.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.external.Firebase.Companion.FCM_SENDER
import com.bopr.android.smailer.sync.SyncWorker.Companion.syncAppDataWithGoogleCloud
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_FORCE_DOWNLOAD
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.slf4j.LoggerFactory

/**
 * Performs actions when specific firebase messages received.
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class FirebaseControlService : FirebaseMessagingService() {

    private val firebase = Firebase(this)

    override fun onMessageReceived(message: RemoteMessage) {
        log.info("Received message: ${message.data}")

        firebase.requestToken { token ->
            if (message.data[FCM_SENDER] == token) {
                log.debug("Ignored self message")
            } else if (message.data[FCM_ACTION] == FCM_REQUEST_DATA_SYNC) {
                syncAppDataWithGoogleCloud(SYNC_FORCE_DOWNLOAD)
            }
        }
    }

    override fun onNewToken(token: String) {
        log.debug("Refreshed token: $token")
        /* do nothing. we are requesting token every time */
    }

    companion object {

        private val log = LoggerFactory.getLogger("FirebaseControlService")
    }
}