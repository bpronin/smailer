package com.bopr.android.smailer.firebase

import android.accounts.Account
import android.content.Context
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.util.getAccount
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.util.*

object CloudMessaging {

    private val log = LoggerFactory.getLogger("CloudMessaging")

    const val FCM_REQUEST_DATA_SYNC = "request_data_sync"
    const val FCM_SENDER = "sender"
    const val FCM_ACTION = "action"

    private val NON_USER_ID_CHARS = Regex("[^a-zA-Z0-9-_~%]")
    private var subscribedTopic: String? = null

    fun Context.subscribeToCloudMessaging() {
        account()?.let { account ->
            subscribedTopic = topic(account).apply {
                FirebaseMessaging.getInstance().subscribeToTopic(this)

                log.debug("Subscribed to: $this")
            }
        } ?: log.debug("No account")
    }

    internal fun unsubscribeFromCloudMessaging() {
        subscribedTopic?.run {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(this)

            log.debug("Unsubscribed from: $this")
            subscribedTopic = null
        }
    }

    fun Context.resubscribeToCloudMessaging() {
        unsubscribeFromCloudMessaging()
        subscribeToCloudMessaging()
    }

    fun Context.sendCloudMessage(action: String) {
        account()?.let { account ->
            requestFirebaseToken { token ->
                val payload = JSONObject().apply {
                    put("to", topic(account))
                    put("data", JSONObject().apply {
                        put(FCM_SENDER, token)
                        put(FCM_ACTION, action)
                    })
                }

                val request = FCMRequest(
                        "https://fcm.googleapis.com/fcm/send",
                        payload,
                        getString(R.string.fcm_server_key),
                        { log.debug("Response: {}", it) },
                        { log.warn("Request failed: ", it) }
                )
                newRequestQueue(this).add(request)

                log.debug("Sent message: {}", payload)
            }
        } ?: log.warn("No account")
    }

    private fun Context.account() = getAccount(Settings(this).senderAccount)

    private fun topic(account: Account) =
            "/topics/com.bopr.android.smailer.firebase-${userId(account.name)}"

    internal fun requestFirebaseToken(onComplete: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            log.debug("Token received")

            onComplete(it.result)
        }
    }

    internal fun Context.listFirebaseInfo(onComplete: (String) -> Unit) {
        requestFirebaseToken { token ->
            val request = FCMRequest(
                    "https://iid.googleapis.com/iid/info/$token?details=true",
                    null,
                    getString(R.string.fcm_server_key),
                    {
                        log.debug("Response: {}", it)
                        onComplete(it.toString(3))
                    },
                    { log.warn("Request failed: ", it) }
            )
            newRequestQueue(this).add(request)
        }
    }

    internal fun userId(email: String): String {
        val parts = email.lowercase(Locale.ROOT).split("@")
        return "${parts[0].replace(NON_USER_ID_CHARS, "")}~${parts[1]}"
    }

    private class FCMRequest(url: String, payload: JSONObject?, val serverKey: String,
                             onComplete: (JSONObject) -> Unit,
                             onError: (VolleyError) -> Unit)
        : JsonObjectRequest(url, payload, onComplete, onError) {

        override fun getHeaders(): Map<String, String> {
            return mapOf(
                    "Authorization" to "key=$serverKey",
                    "Content-Type" to "application/json"
            )
        }
    }
}