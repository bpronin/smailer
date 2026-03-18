package com.bopr.android.smailer.messenger.pocketbase

import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.EventPayload
import com.bopr.android.smailer.provider.battery.BatteryData
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.Logger
import okhttp3.Interceptor
import okhttp3.OkHttpClient.Builder
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.time.Instant

class PocketbaseClient(baseUrl: String) {

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl.let { if (it.endsWith("/")) it else "$it/" })
        .client(
            Builder()
                .addInterceptor(AuthInterceptor())
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: PocketbaseApi = retrofit.create(PocketbaseApi::class.java)
    private var authToken: String? = null

    suspend fun auth(user: String, password: String) {
        log.debug("Auth as: $user")
        val response = api.auth(AuthRequest(user, password))
        if (response.isSuccessful) {
            authToken = response.body()?.token
            log.debug("Auth success")
        } else {
            log.debug("Auth failed")
            throw PocketbaseRemoteError("Auth failed", parseErrorResponse(response))
        }
    }

    suspend fun insertEvent(event: Event): String? {
        if (authToken == null) throw IllegalStateException("Not authenticated")

        val eventId = insertIntoEvents(event) ?: return null
        when (val payload = event.payload) {
            is PhoneCallData -> insertIntoTelephony(eventId, payload)
            is BatteryData -> insertIntoBattery(eventId, payload)
            else -> throw IllegalArgumentException("Unknown payload type: ${event.payload}")
        }
        return eventId
    }

    private suspend fun insertIntoEvents(event: Event): String? {
        log.debug("Inserting into 'events'")

        val response = api.insertEvent(
            InsertEventRequest(
                time = formatDateTime(event.time),
                target = event.target,
                type = formatPayloadType(event.payload)
            )
        )

        return if (response.isSuccessful) {
            response.body()?.id
        } else {
            throw PocketbaseRemoteError("Insert failed", parseErrorResponse(response))
        }
    }

    private suspend fun insertIntoTelephony(eventId: String, data: PhoneCallData) {
        log.debug("Inserting into 'telephony'")

        val response = api.insertTelephony(
            InsertTelephonyRequest(
                event_id = eventId,
                start_time = formatDateTime(data.startTime),
                end_time = formatDateTime(data.startTime),
                phone = data.phone,
                is_incoming = data.isIncoming,
                is_missed = data.isMissed,
                text = data.text
            )
        )

        if (!response.isSuccessful) {
            throw PocketbaseRemoteError("Insert failed", parseErrorResponse(response))
        }
    }

    private suspend fun insertIntoBattery(eventId: String, data: BatteryData) {
        log.debug("Inserting into 'battery'")

        val response = api.insertBattery(
            InsertBatteryRequest(
                event_id = eventId,
                level = data.level
            )
        )

        if (!response.isSuccessful) {
            throw PocketbaseRemoteError("Insert failed", parseErrorResponse(response))
        }
    }

    private fun formatPayloadType(payload: EventPayload) = when (payload) {
        is PhoneCallData -> "telephony"
        is BatteryData -> "battery"
        else -> throw IllegalArgumentException("Unknown payload type: $payload")
    }

    private fun formatDateTime(ms: Long) = Instant.fromEpochMilliseconds(ms).toString()

    private fun parseErrorResponse(response: Response<*>) =
        retrofit.responseBodyConverter<ErrorResponse>(ErrorResponse::class.java, arrayOf())
            .convert(response.errorBody())!!

    inner class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request().newBuilder().apply {
                authToken?.let { addHeader("Authorization", "Bearer $it") }
            }.build()
            return chain.proceed(request)
        }
    }

    companion object {
        private val log = Logger("PocketbaseClient")
    }
}