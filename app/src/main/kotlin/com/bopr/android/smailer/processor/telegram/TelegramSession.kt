package com.bopr.android.smailer.processor.telegram

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.NetworkError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_CONNECTION
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
        oldChatId: String?,
        onSuccess: (chatId: String) -> Unit = {},
        onError: (error: TelegramException) -> Unit = {}
    ) {
        requestChat(
            oldChatId,
            onSuccess = { chatId ->
                requestSendMessage(chatId, message, onSuccess, onError)
            },
            onError = { error ->
                if (error.code == TELEGRAM_NO_CHAT) {
                    requestUpdates(
                        onSuccess = { chatId ->
                            requestSendMessage(chatId, message, onSuccess, onError)
                        },
                        onError = onError
                    )
                } else {
                    onError(error)
                }
            }
        )
    }

    private fun requestChat(
        chatId: String?,
        onSuccess: (String) -> Unit,
        onError: (error: TelegramException) -> Unit
    ) {
        if (chatId.isNullOrEmpty()) {
            onError(TelegramException(TELEGRAM_NO_CHAT, "Chat ID is null"))
            return
        }

        request(
            command = "getChat?chat_id=$chatId",
            onResponse = { response ->
                detectRemoteError(response, TELEGRAM_NO_CHAT)

                onSuccess(
                    response
                        .getJSONObject("result")
                        .getString("id")
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
                detectRemoteError(response, TELEGRAM_BAD_RESPONSE)

                val data = response.getJSONArray("result")
                if (data.length() == 0)
                    throw TelegramException(TELEGRAM_NO_CHAT, "Empty updates")

                val chatId = data.getJSONObject(0)
                    .getJSONObject("message")
                    .getJSONObject("chat")
                    .getString("id")

                onSuccess(chatId)
            },
            onError = onError
        )
    }

    private fun requestSendMessage(
        chatId: String,
        message: String,
        onSuccess: (String) -> Unit,
        onError: (TelegramException) -> Unit
    ) {
        request(
            command = "sendMessage?" +
                    "chat_id=$chatId&" +
                    "text=${fixMessageText(message)}&" +
                    "parse_mode=$PARSE_MODE",
            onResponse = { response ->
                detectRemoteError(response, TELEGRAM_BAD_RESPONSE)
                onSuccess(chatId)
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
            onError(TelegramException(TELEGRAM_NO_TOKEN, "Token is null"))
        } else {
            val url = "$BASE_URL$token/$command"
            val request = JsonObjectRequest(url,
                { response ->
                    try {
                        onResponse(response)
                    } catch (x: TelegramException) {
                        log.error("Error while processing <$command> response.", x)

                        onError(x)
                    }
                },
                { error ->
                    log.error("Error while sending <$command> request.", error)

                    when (error) {
                        is AuthFailureError ->
                            onError(
                                TelegramException(
                                    TELEGRAM_INVALID_TOKEN,
                                    error.localizedMessage.orEmpty(), error
                                )
                            )

                        is NetworkError,
                        is TimeoutError ->
                            onError(
                                TelegramException(
                                    TELEGRAM_NO_CONNECTION,
                                    error.localizedMessage.orEmpty(), error
                                )
                            )

                        else ->
                            onError(
                                TelegramException(
                                    TELEGRAM_REQUEST_FAILED,
                                    error.localizedMessage.orEmpty(), error
                                )
                            )
                    }
                }
            )

            requestQueue.add(request)
        }
    }

    private fun detectRemoteError(
        response: JSONObject,
        exceptionCode: TelegramException.Code
    ) {
        if (!response.getBoolean("ok")) {
            throw TelegramRemoteError(
                exceptionCode,
                response.getInt("error_code"), response.getString("description")
            )
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
