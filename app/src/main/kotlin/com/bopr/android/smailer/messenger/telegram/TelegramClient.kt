package com.bopr.android.smailer.messenger.telegram

import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_NO_UPDATES
import com.bopr.android.smailer.util.Logger
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TelegramClient(token: String) {

    private val errorConverter: Converter<ResponseBody, ErrorResponse>
    private val api = Retrofit.Builder()
        .baseUrl("https://api.telegram.org/bot$token/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .also {
            errorConverter = it.responseBodyConverter(ErrorResponse::class.java, arrayOf())
        }
        .create(TelegramApi::class.java)

    suspend fun send(message: String, oldChatId: String?): String? {
        val chatId = if (isChatExists(oldChatId)) oldChatId else getChatIdFromUpdates()
        chatId?.let { sendMessage(it, message) }
        return chatId
    }

    private suspend fun isChatExists(chatId: String?): Boolean {
        log.debug("Checking chat existence")

        if (chatId.isNullOrEmpty()) return false

        val response = api.getChat(chatId)
        detectErrorResponse(response)
        return response.body()?.ok ?: false
    }

    private suspend fun getChatIdFromUpdates(): String {
        log.debug("Getting chat id from updates")

        val response = api.getUpdates()

        detectErrorResponse(response)

        val result = response.body()!!.result
        if (result.isEmpty()) throw TelegramException(TELEGRAM_NO_UPDATES, "Empty updates")
        return result.first().message.chat.id
    }

    private suspend fun sendMessage(chatId: String, message: String) {
        log.debug("Sending message")

        val response = api.sendMessage(chatId, message, "HTML")
        detectErrorResponse(response)
    }

    private fun detectErrorResponse(response: Response<*>) {
        if (!response.isSuccessful) {
            val error = errorConverter.convert(response.errorBody()!!)!!
            log.warn("Error response: $error")

            val code = when (error.error_code) {
                401 -> TELEGRAM_INVALID_TOKEN
                else -> TELEGRAM_BAD_RESPONSE
            }

            throw TelegramException(code, error.description)
        }
    }

    companion object {
        private val log = Logger("TelegramClient")
    }
}