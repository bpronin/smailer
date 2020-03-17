package com.bopr.android.smailer.firebase

import com.bopr.android.smailer.firebase.CloudMessaging.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.firebase.CloudMessaging.requestFirebaseToken
import com.bopr.android.smailer.sync.SyncWorker.Companion.requestDataSync
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.slf4j.LoggerFactory


class CloudMessagingService : FirebaseMessagingService() {

    private var instanceToken: String? = null

    private val log = LoggerFactory.getLogger("CloudMessagingService")

    override fun onMessageReceived(message: RemoteMessage) {
        log.info("Received message: ${message.data}")

        loadInstanceToken { token ->
            if (message.data["sender"] != token) {
                if (message.data["action"] == FCM_REQUEST_DATA_SYNC) {
                    requestDataSync()
                }
            } else {
                log.debug("Ignored self message")
            }
        }
    }

    override fun onNewToken(token: String) {
        instanceToken = token
        log.debug("Refreshed token: $token")
    }

    private fun loadInstanceToken(onComplete: (String) -> Unit) {
        instanceToken?.run(onComplete) ?: requestFirebaseToken {
            instanceToken = it
            onComplete(it)
        }
    }
}