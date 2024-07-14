package com.bopr.android.smailer.external

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_REQUEST_FAILED
import com.bopr.android.smailer.util.Mockable
import com.google.common.base.Strings.isNullOrEmpty
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * Telegram bot wrapper.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@Mockable
internal class Telegram(context: Context) {

    private val settings = Settings(context)
    private val requestQueue = Volley.newRequestQueue(context)

    fun sendMessage(
        message: String,
        onSuccess: () -> Unit = {},
        onError: (error: Exception) -> Unit = {}
    ) {
        requestUpdates(
            onSuccess = { chatId ->
                requestSendMessage(
                    chatId, message,
                    onSuccess = onSuccess,
                    onError = onError
                )
            },
            onError = onError
        )
    }

    private fun requestUpdates(
        onSuccess: (chatId: String) -> Unit,
        onError: (error: Exception) -> Unit
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

                        log.debug("Chat ID = $chatId")

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
        onError: (error: Exception) -> Unit
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
        onError: (error: Exception) -> Unit
    ) {
        val token = settings.getTelegramBotToken()

        if (isNullOrEmpty(token)) {
            onError(TelegramException(TELEGRAM_NO_TOKEN))
        } else {
            val url = "$BASE_URL$token/$command"
            val request = JsonObjectRequest(url,
                { response ->
                    try {
                        onResponse(response)
                    } catch (x: Exception) {
                        log.error("Error while processing <$command> response.", x)

                        onError(x)
                    }
                },
                { error ->
                    log.error("<$command> request failed.", error)

                    if (error is AuthFailureError) {
                        onError(TelegramException(TELEGRAM_INVALID_TOKEN, error))
                    } else {
                        onError(TelegramException(TELEGRAM_REQUEST_FAILED, error))
                    }
                }
            )

            requestQueue.add(request)
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("Telegram")
        private const val BASE_URL = "https://api.telegram.org/bot"
        private const val PARSE_MODE = "html"
    }

}
