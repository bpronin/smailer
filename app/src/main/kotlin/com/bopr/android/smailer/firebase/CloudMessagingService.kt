package com.bopr.android.smailer.firebase

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.slf4j.LoggerFactory

class CloudMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        log.info("Priority: ${message.priority}")
        log.info("Notification priority: ${message.notification?.notificationPriority}")
        log.info("Received: ${message.notification}")
    }

    override fun onNewToken(token: String) {
        log.debug("Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO
    }

    companion object {

        private val log = LoggerFactory.getLogger("CloudMessagingService")

        fun getFirebaseCurrentToken(action: (token: String?) -> Unit) {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token

                    log.info("Current token: $token")

                    action(token)
                } else {
                    log.warn("getInstanceId failed", task.exception)
                }
            }
        }
    }

}