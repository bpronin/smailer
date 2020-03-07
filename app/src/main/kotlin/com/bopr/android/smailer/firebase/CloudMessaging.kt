package com.bopr.android.smailer.firebase

import android.content.Context
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bopr.android.smailer.R
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONObject
import org.slf4j.LoggerFactory


class CloudMessaging(context: Context) {

    private val log = LoggerFactory.getLogger("CloudMessaging")
    private val serverKey = context.getString(R.string.fcm_server_key)
    private val requestQueue = Volley.newRequestQueue(context)

//    private const val TOPIC = "com.bopr.android.smailer.firebase"

//    fun subscribeToFirebaseMessaging() {
//        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
//
//        log.debug("Subscribed to: $TOPIC")
//    }

    fun sendFirebaseSelfMessage() {
//            put("to", "/topics/$TOPIC")
//            put("to", "fRCwdvebsDs:APA91bE3dJydwx_gSS7al1GE9-ptQWXh4F1z-861U_Wn_T-RPkw-7k5fkCA8jyZbX3D9Sxi4dR-Uzxmj4L8vpnktzH4bFF3yzjeXKxLkpsQD482zzaki_betfcvGobclxmpTH4eoQ556")
//            put("to", "c_upLLVOgrg:APA91bEUglbusjtb0p67WYoxlYDbPLaRyak1WSSmiPUpK1h-ZwHbgmMimVtJf3JsIvR6g5EtKVxDlUCG9qDRwZ4YnMpZ8LOpAn7gOoDgm7CngDNtqsLHgFJL2RnspbS6DFw7jjEtGstL")
        requestCurrentFirebaseToken {
            sendFirebaseMessage(it!!)
        }
    }

    fun requestCurrentFirebaseToken(onComplete: (String?) -> Unit) {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token

                log.info("Current token: $token")

                onComplete(token)
            } else {
                log.warn("Failed", task.exception)
            }
        }
    }

    fun sendFirebaseMessage(to: String) {
        val payload = JSONObject().apply {
            put("to", to)
            put("data", JSONObject().apply {
                put("message", "Hello")
            })
            put("android", JSONObject().apply {
                put("priority", "high")
            })
        }

        val request = FCMRequest(payload = payload,
                onResponse = {
                    log.debug("Response: $it")
                },
                onError = {
                    log.warn("Request error: ", it)
                }
        )

        requestQueue.add(request)
    }

    private inner class FCMRequest(payload: JSONObject, onResponse: (JSONObject) -> Unit,
                                   onError: (VolleyError) -> Unit)
        : JsonObjectRequest("https://fcm.googleapis.com/fcm/send", payload, onResponse, onError) {

        override fun getHeaders(): Map<String, String> {
            return mapOf(
                    "Authorization" to "key=$serverKey",
                    "Content-Type" to "application/json"
            )
        }
    }
}