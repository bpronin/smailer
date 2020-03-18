package com.bopr.android.smailer.firebase

import com.bopr.android.smailer.firebase.CloudMessaging.FCM_ACTION
import com.bopr.android.smailer.firebase.CloudMessaging.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.firebase.CloudMessaging.FCM_SENDER
import com.bopr.android.smailer.firebase.CloudMessaging.requestFirebaseToken
import com.bopr.android.smailer.sync.SyncWorker.Companion.requestDataSync
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_FORCE_DOWNLOAD
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.slf4j.LoggerFactory


class CloudMessagingService : FirebaseMessagingService() {

    private val log = LoggerFactory.getLogger("CloudMessagingService")

    override fun onMessageReceived(message: RemoteMessage) {
        log.info("Received message: ${message.data}")

        requestFirebaseToken { token ->
            if (message.data[FCM_SENDER] != token) {
                if (message.data[FCM_ACTION] == FCM_REQUEST_DATA_SYNC) {
                    requestDataSync(SYNC_FORCE_DOWNLOAD)
                }
            } else {
                log.debug("Ignored self message")
            }
        }
    }

    override fun onNewToken(token: String) {
        log.debug("Refreshed token: $token")
        /* do nothing. we are requesting token every time */
    }
}