package com.bopr.android.smailer.firebase

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.util.getAccount
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import org.slf4j.LoggerFactory

object CloudMessaging {

    private val log = LoggerFactory.getLogger("CloudMessaging")
    const val FCM_REQUEST_DATA_SYNC = "request_data_sync"
    private var topic: String? = null

    fun Context.subscribeToCloudMessaging() {
        getAccount(Settings(this).getString(PREF_SENDER_ACCOUNT))?.let { account ->
            val userId = account.name.replace("@", "_")
            topic = "/topics/com.bopr.android.smailer.firebase-$userId"

            topic!!.let {
                FirebaseMessaging.getInstance().subscribeToTopic(it)

                log.debug("Subscribed to: $it")
            }
        }
    }

    fun unsubscribeFromCloudMessaging() {
        topic?.let {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(it)

            log.debug("Unsubscribed from: $it")
        }
    }

    fun Context.resubscribeToCloudMessaging() {
        unsubscribeFromCloudMessaging()
        subscribeToCloudMessaging()
    }

    fun Context.sendCloudMessage(action: String) {
        topic?.let {
            requestFirebaseToken { token ->   // todo put token into settings
                val payload = JSONObject().apply {
                    put("to", it)
                    put("data", JSONObject().apply {
                        put("action", action)
                        put("sender", token)
                    })
                }

                val request = FCMRequest(payload, getString(R.string.fcm_server_key))
                newRequestQueue(this).add(request)

                log.debug("Sent message: $payload")
            }
        } ?: log.warn("Unsubscribed")
    }

    fun requestFirebaseToken(onComplete: (String) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result!!.token

                log.info("Current token: $token")
                onComplete(token)
            } else {
                log.warn("Failed", task.exception)
            }
        }
    }

    private class FCMRequest(payload: JSONObject, val serverKey: String)
        : JsonObjectRequest(
            "https://fcm.googleapis.com/fcm/send",
            payload,
            { log.debug("Response: $it") },
            { log.warn("Request failed: ", it) }
    ) {

        override fun getHeaders(): Map<String, String> {
            return mapOf(
                    "Authorization" to "key=$serverKey",
                    "Content-Type" to "application/json"
            )
        }
    }
}