package com.bopr.android.smailer.messenger.pocketbase

import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.pocketbase.PocketbaseException.Code.POCKETBASE_BAD_RESPONSE
import com.bopr.android.smailer.messenger.pocketbase.PocketbaseException.Code.POCKETBASE_BAD_CREDENTIALS
import com.bopr.android.smailer.provider.battery.BatteryData
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.Logger
import okhttp3.Interceptor
import okhttp3.OkHttpClient.Builder
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory.create
import kotlin.time.Instant

class PocketbaseClient(baseUrl: String) {

    private val errorConverter: Converter<ResponseBody, ErrorResponse>
    private val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(
            Builder()
                .addInterceptor(AuthInterceptor())
                .build()
        )
        .addConverterFactory(create())
        .build()
        .also {
            errorConverter = it.responseBodyConverter(ErrorResponse::class.java, arrayOf())
        }
        .create(PocketbaseApi::class.java)

    private var authToken: String? = null

    suspend fun auth(user: String, password: String) {
        log.debug("Authorizing")
        
        val response = api.auth(AuthRequest(user, password))
        if (!response.isSuccessful) {
            val error = parseErrorResponse(response)
            log.warn("Auth failed: $error")
            throw PocketbaseException(POCKETBASE_BAD_CREDENTIALS, "$response")
        }
        
        authToken = response.body()?.token
        log.info("Authorized")
    }

    suspend fun insertEvent(event: Event): String {
        if (authToken == null) throw IllegalStateException("Not authenticated")

        val eventId = insertIntoEvents(event)
        when (val payload = event.payload) {
            is PhoneCallData -> insertIntoTelephony(eventId, payload)
            is BatteryData -> insertIntoBattery(eventId, payload)
            else -> throw IllegalArgumentException("Unknown payload type: ${event.payload}")
        }
        return eventId
    }

    private suspend fun insertIntoEvents(event: Event): String {
        log.debug("Inserting into 'events'")

        val response = api.insertEvent(
            InsertEventRequest(
                time = formatDateTime(event.time),
                target = event.target,
                type = when (event.payload) {
                    is PhoneCallData -> "telephony"
                    is BatteryData -> "battery"
                    else -> throw IllegalArgumentException("Unknown payload type: ${event.payload}")
                },
                location = event.location?.let {
                    PocketbaseLocation(it.latitude, it.longitude)
                }
            )
        )
        if (!response.isSuccessful) {
            val error = parseErrorResponse(response)
            log.warn("Insert failed: $error")
            throw PocketbaseException(POCKETBASE_BAD_RESPONSE, "$response")
        }
        return response.body()!!.id
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
            val error = parseErrorResponse(response)
            log.warn("Insert failed: $error")
            throw PocketbaseException(POCKETBASE_BAD_RESPONSE, "$response")
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
            val error = parseErrorResponse(response)
            log.warn("Insert failed: $error")
            throw PocketbaseException(POCKETBASE_BAD_RESPONSE, "$response")
        }
    }

    private fun formatDateTime(ms: Long) = Instant.fromEpochMilliseconds(ms).toString()

    private fun parseErrorResponse(response: Response<*>): ErrorResponse =
        errorConverter.convert(response.errorBody()!!)!!

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