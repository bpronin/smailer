package com.bopr.android.smailer.external

import android.accounts.Account
import android.content.Context
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.SingletonHolder
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import java.util.Locale

class Firebase private constructor(val context: Context) {

    private var subscribedTopic: String? = null
    private val firebaseMessaging = FirebaseMessaging.getInstance()
    private val requestQueue = Volley.newRequestQueue(context)

    internal fun subscribe() {
        getAccount()?.let { account ->
            subscribedTopic = formatTopic(account).also {
                firebaseMessaging.subscribeToTopic(it)

                log.debug("Subscribed to topic").verb(it)
            }
        } ?: log.debug("No account")
    }

    internal fun unsubscribe() {
        subscribedTopic?.run {
            firebaseMessaging.unsubscribeFromTopic(this)

            log.debug("Unsubscribed from topic").verb(this)

            subscribedTopic = null
        }
    }

    internal fun send(action: String) {
        getAccount()?.let { account ->
            requestToken { token ->
                val payload = JSONObject().apply {
                    put("to", formatTopic(account))
                    put("data", JSONObject().apply {
                        put(FCM_SENDER, token)
                        put(FCM_ACTION, action)
                    })
                }

                val request = FirebaseRequest("https://fcm.googleapis.com/fcm/send", payload)
                requestQueue.add(request)

                log.debug("Sent message").verb(payload)
            }
        } ?: log.warn("No account")
    }

    internal fun requestToken(onComplete: (String) -> Unit) {
        firebaseMessaging.token.addOnCompleteListener {
            log.debug("Token received").verb(it.result)

            onComplete(it.result)
        }
    }

    internal fun requestInfo(onComplete: (String) -> Unit) {
        requestToken { token ->
            val request = FirebaseRequest(
                url = "https://iid.googleapis.com/iid/info/$token?details=true",
                onComplete = { response ->
                    onComplete(response.toString(3))
                }
            )
            requestQueue.add(request)
        }
    }

    private fun getAccount() =
        context.run {
            accounts.getGoogleAccount(
                settings.getString(PREF_MAIL_SENDER_ACCOUNT)
            )
        }


    private fun formatUserId(email: String): String {
        val parts = email.lowercase(Locale.ROOT).split("@")
        val firstPart = parts[0].replace(Regex("[^a-zA-Z0-9-_~%]"), "")
        val lastPart = parts[1]
        return "$firstPart~$lastPart"
    }

    private fun formatTopic(account: Account): String {
        val userId = formatUserId(account.name)
        return "/topics/com.bopr.android.smailer.firebase-$userId"
    }

    private inner class FirebaseRequest(
        url: String,
        payload: JSONObject? = null,
        onComplete: (JSONObject) -> Unit = {},
        onError: (VolleyError) -> Unit = {}
    ) : JsonObjectRequest(Method.POST, url, payload,
        { response ->
            log.debug("Response").verb(response)

            onComplete(response)
        },
        { error ->
            log.warn("Request failed", error)

            onError(error)
        }) {

        override fun getHeaders(): Map<String, String> {
            return mapOf(
                "Authorization" to "key=${context.getString(R.string.fcm_server_key)}",
                "Content-Type" to "application/json"
            )
        }
    }

    companion object {

        private val log = Logger("Firebase")

        const val FCM_REQUEST_DATA_SYNC = "request_data_sync"
        const val FCM_SENDER = "sender"
        const val FCM_ACTION = "action"

        private val singletonHolder = SingletonHolder { Firebase(it) }
        val Context.firebase get() = singletonHolder.getInstance(this)
    }
}