package com.bopr.android.smailer.firebase

import com.bopr.android.smailer.PendingCallProcessorWorker.Companion.startPendingCallProcessorService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.slf4j.LoggerFactory


class CloudMessagingService : FirebaseMessagingService() {

    private val log = LoggerFactory.getLogger("CloudMessagingService")

    override fun onMessageReceived(message: RemoteMessage) {
        log.info("Priority: ${message.priority}")
        log.info("Data: ${message.data}")

        if (message.data["action"] == "wakeup") {
            startPendingCallProcessorService(this)
        }
    }

    override fun onNewToken(token: String) {
        log.debug("Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        // sendRegistrationToServer(token)

        /*
          This method is invoked whenever the token refreshes
        */

        // Once the token is generated, subscribe to topic with the userId
        //subscribeToFirebaseMessaging()
    }

    override fun onMessageSent(messageId: String) {
        log.debug("Sent: $messageId")
    }

    override fun onDeletedMessages() {
        log.debug("Deleted")
    }

    override fun onSendError(messageId: String, exception: Exception) {
        log.debug("Send message: $messageId Error: $exception")
    }

}