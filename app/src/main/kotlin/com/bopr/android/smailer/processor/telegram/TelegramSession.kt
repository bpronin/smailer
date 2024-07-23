package com.bopr.android.smailer.processor.telegram

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_REQUEST_FAILED
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.net.URLEncoder

/**
 * Telegram bot wrapper.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramSession(context: Context, private val token: String?) {

    private val requestQueue = Volley.newRequestQueue(context)

    fun sendMessage(
        message: String,
        onSuccess: () -> Unit = {},
        onError: (error: TelegramException) -> Unit = {}
    ) {
        requestUpdates(
            onSuccess = { chatId ->
                requestSendMessage(
                    chatId = chatId,
                    text = fixMessageText(message),
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onError = onError
        )
    }

    private fun requestUpdates(
        onSuccess: (chatId: String) -> Unit,
        onError: (error: TelegramException) -> Unit
    ) {
        request(
            command = "getUpdates",
            onResponse = { response ->
                if (!response.getBoolean("ok")) {
                    throw TelegramException(TELEGRAM_BAD_RESPONSE)
                } else {
                    val updates = response.getJSONArray("result")
                    if (updates.length() == 0) {
                        throw TelegramException(TELEGRAM_NO_CHAT)
                    } else {
                        val chatId = updates.getJSONObject(0)
                            .getJSONObject("message")
                            .getJSONObject("chat")
                            .getString("id")

                        onSuccess(chatId)
                    }
                }
            },
            onError = onError
        )
    }

    private fun requestSendMessage(
        chatId: String,
        text: String,
        onSuccess: () -> Unit,
        onError: (error: TelegramException) -> Unit
    ) {
        request(
            command = "sendMessage?" +
                    "chat_id=$chatId&" +
                    "text=$text&" +
                    "parse_mode=$PARSE_MODE",
            onResponse = { response ->
                if (!response.getBoolean("ok")) {
                    throw TelegramException(TELEGRAM_BAD_RESPONSE)
                } else {
                    onSuccess()
                }
            },
            onError = onError
        )
    }

    private fun request(
        command: String,
        onResponse: (response: JSONObject) -> Unit,
        onError: (error: TelegramException) -> Unit
    ) {
        if (token.isNullOrEmpty()) {
            onError(TelegramException(TELEGRAM_NO_TOKEN))
        } else {
            val url = "$BASE_URL$token/$command"
            val request = JsonObjectRequest(url,
                { response ->
                    try {
                        onResponse(response)
                    } catch (x: Throwable) {
                        log.error("Error while processing <$command> response.", x)

                        onError(TelegramException(TELEGRAM_REQUEST_FAILED, x))
                    }
                },
                { error ->
                    log.error("<$command> request failed.", error)

                    when (error) {
                        is AuthFailureError ->
                            onError(TelegramException(TELEGRAM_INVALID_TOKEN, error))

                        else ->
                            onError(TelegramException(TELEGRAM_REQUEST_FAILED, error))
                    }
                }
            )

            requestQueue.add(request)
        }
    }

    private fun fixMessageText(text: String): String {
        return text.replace("\n", NEW_LINE)
    }

    companion object {

        private val log = LoggerFactory.getLogger("Telegram")

        private val NEW_LINE = URLEncoder.encode("\n", "UTF-8")
        private const val BASE_URL = "https://api.telegram.org/bot"
        private const val PARSE_MODE = "html"
    }

}
