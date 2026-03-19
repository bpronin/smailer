@file:Suppress("PropertyName", "LocalVariableName")

package com.bopr.android.smailer.messenger.telegram

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TelegramApi {

    @GET("getChat")
    suspend fun getChat(
        @Query("chat_id") chat_id: String?,
    ): Response<OkResponse>

    @GET("getUpdates")
    suspend fun getUpdates(
    ): Response<UpdatesResponse>
    
    @GET("sendMessage")
    suspend fun sendMessage(
        @Query("chat_id") chat_id: String,
        @Query("text") message: String,
        @Query("parse_mode") parse_mode: String
    ): Response<OkResponse>

}

data class ErrorResponse(val error_code: Int, val description: String)
data class OkResponse(val ok: Boolean)

data class UpdatesChat(val id: String)
data class UpdatesMessage(val chat: UpdatesChat)
data class UpdatesResult(val message: UpdatesMessage)
data class UpdatesResponse(val result: List<UpdatesResult>)