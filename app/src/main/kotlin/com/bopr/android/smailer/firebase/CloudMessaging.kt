package com.bopr.android.smailer.firebase

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bopr.android.smailer.R
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import org.slf4j.LoggerFactory


object CloudMessaging {

    private val log = LoggerFactory.getLogger("CloudMessaging")

    private const val FCM_API_URL = "https://fcm.googleapis.com/fcm/send"
    private const val TOPIC = "com.bopr.android.smailer.firebase"

    fun subscribeToFirebaseMessaging() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        log.debug("Subscribed to: $TOPIC")
    }

    fun requestCurrentFirebaseToken(action: (String?) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token

                log.info("Current token: $token")

                action(token)
            } else {
                log.warn("Failed", task.exception)
            }
        }
    }

    fun sendFirebaseMessage(context: Context) {
        val serverKey = context.getString(R.string.fcm_server_key)

        val payload = JSONObject().apply {
            put("to", "/topics/$TOPIC")
            put("data", JSONObject().apply {
                put("title", "Title")
                put("message", "Message")
            })
        }

        val request = FCMRequest(payload, serverKey) {
            log.debug("Response: $it")
        }

        val queue = Volley.newRequestQueue(context)
        queue.add(request)
    }

    private class FCMRequest(payload: JSONObject, private val serverKey: String, onResponse: (JSONObject) -> Unit)
        : JsonObjectRequest(
            FCM_API_URL,
            payload,
            onResponse,
            {
                log.warn("Request error: ", it)
            }
    ) {

        override fun getHeaders(): Map<String, String> {
            return mapOf(
                    "Authorization" to "key=$serverKey",
                    "Content-Type" to "application/json"
            )
        }
    }
}