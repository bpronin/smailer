@file:Suppress("PropertyName")

package com.bopr.android.smailer.messenger.pocketbase

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PocketbaseApi {

    @POST("api/collections/_superusers/auth-with-password")
    suspend fun auth(
        @Body body: AuthRequest,
    ): Response<AuthResponse>

    @POST("api/collections/events/records")
    suspend fun insertEvent(
        @Body body: InsertEventRequest
    ): Response<InsertResponse>

    @POST("api/collections/telephony/records")
    suspend fun insertTelephony(
        @Body body: InsertTelephonyRequest
    ): Response<InsertResponse>

    @POST("api/collections/battery/records")
    suspend fun insertBattery(
        @Body body: InsertBatteryRequest
    ): Response<InsertResponse>

}

data class ErrorResponse(val status: Int, val message: String)

data class AuthRequest(val identity: String, val password: String)

data class AuthResponse(val token: String)

data class InsertEventRequest(
    val time: String,
    val target: String,
    val type: String,
    val location: PocketbaseLocation? = null,
)

data class InsertTelephonyRequest(
    val event_id: String,
    val start_time: String,
    val end_time: String,
    val phone: String,
    val is_incoming: Boolean,
    val is_missed: Boolean,
    val text: String?
)

data class InsertBatteryRequest(
    val event_id: String,
    val level: String
)

data class InsertResponse(val id: String)

data class PocketbaseLocation(val lat: Double, val lon: Double)
