package com.bopr.android.smailer.messenger.pocketbase

import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.EventPayload
import com.bopr.android.smailer.provider.battery.BatteryData
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import kotlin.time.Instant

class PocketbaseClient(private val baseUrl: String) {

    private var authToken: String? = null
    private val client = OkHttpClient().newBuilder().addInterceptor(AuthInterceptor()).build()

    suspend fun auth(identity: String, password: String, isSuperuser: Boolean = false) =
        withContext(Dispatchers.IO) {
            val json = """
            {
                "identity": "$identity",
                "password": "$password"
            }
            """

            val collection = if (isSuperuser) "_superusers" else "users"
            val request = Request.Builder()
                .url(collectionUrl("$collection/auth-with-password"))
                .post(jsonBody(json))
                .build()

            authToken = null
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = JSONObject(response.body.string())
                    authToken = json.getString("token")
                    log.debug("Auth success")
                } else {
                    throw RuntimeException("Auth failed: $response")
                }
            }
        }

    suspend fun insertEvent(event: Event): String? =
        insertIntoEvents(event)?.also {
            when (event.payload) {
                is PhoneCallData -> insertIntoTelephony(it, event.payload)
                is BatteryData -> insertIntoBattery(it, event.payload)
                else -> throw IllegalArgumentException("Unknown payload type: ${event.payload}")
            }
        }

    suspend fun insertIntoTelephony(eventId: String, data: PhoneCallData): String? =
        insertInto(
            "telephony",
            """
            {
                "event_id": "$eventId",
                "start_time": "${formatDateTime(data.startTime)}",
                "end_time": "${formatDateTime(data.startTime)}",
                "phone": "${data.phone}",
                "is_incoming": ${data.isIncoming},
                "is_missed": ${data.isMissed},
                "text": "${data.text}"
            }
            """
        )

    suspend fun insertIntoBattery(eventId: String, data: BatteryData) = insertInto(
        "battery",
        """
        {
            "event_id": "$eventId",
            "level": ${data.level},
        }    
        """
    )

    suspend fun insertIntoEvents(event: Event) = insertInto(
        "events",
        """
        {
            "time": "${formatDateTime(event.time)}",
            "target": "${event.target}",
            "type": "${formatPayloadType(event.payload)}"    
        }
        """
    )

    private suspend fun insertInto(collection: String, json: String): String? =
        withContext(Dispatchers.IO) {
            if (authToken == null) throw IllegalStateException("Not authenticated")

            val request = Request.Builder()
                .url(collectionUrl("${collection}/records"))
                .post(jsonBody(json))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    log.debug("Insert into '$collection' success")
                    val json = JSONObject(response.body.string())
                    json.getString("id")
                } else {
                    throw RuntimeException("Insert into '$collection' failed: ${response.body.string()}")
                }
            }
        }

    private fun jsonBody(json: String): RequestBody =
        json.toRequestBody("application/json".toMediaType())

    private fun collectionUrl(path: String): String = "$baseUrl/api/collections/$path"

    private fun formatPayloadType(payload: EventPayload): String {
        return when (payload) {
            is PhoneCallData -> "telephony"
            is BatteryData -> "battery"
            else -> throw IllegalArgumentException("Unknown payload type: $payload")
        }
    }

    private fun formatDateTime(ms: Long): String =
        Instant.fromEpochMilliseconds(ms).toString()

    private fun parseDateTime(s: String): Long = Instant.parse(s).toEpochMilliseconds()

    inner class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {

            val request = chain.request().newBuilder().apply {
                authToken?.let {
                    addHeader("Authorization", "Bearer $it")
                }
            }.build()

            return chain.proceed(request)
        }
    }

    companion object {

        private val log = Logger("PocketbaseClient")
    }
}