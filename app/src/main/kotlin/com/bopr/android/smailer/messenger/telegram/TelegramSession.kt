package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.android.volley.AuthFailureError
import com.android.volley.NetworkError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_NO_CONNECTION
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_NO_UPDATES
import com.bopr.android.smailer.messenger.telegram.TelegramException.Code.TELEGRAM_REQUEST_FAILED
import com.bopr.android.smailer.util.Logger
import org.json.JSONObject
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
            command = "getChat",
            params = mapOf("chat_id" to chatId),
            onResponse = {
                detectRemoteError(it, TELEGRAM_NO_CHAT)
                onSuccess(it.getJSONObject("result").getString("id"))
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
            onResponse = {
                detectRemoteError(it, TELEGRAM_BAD_RESPONSE)

                val data = it.getJSONArray("result")
                if (data.length() == 0)
                    throw TelegramException(TELEGRAM_NO_UPDATES, "Empty updates")

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
            command = "sendMessage",
            params = mapOf(
                "chat_id" to chatId,
                "text" to fixMessageText(message),
                "parse_mode" to PARSE_MODE
            ),
            onResponse = {
                detectRemoteError(it, TELEGRAM_BAD_RESPONSE)
                onSuccess(chatId)
            },
            onError = onError
        )
    }

    private fun request(
        command: String,
        params: Map<String, Any> = emptyMap<String, Any>(),
        onResponse: (JSONObject) -> Unit,
        onError: (TelegramException) -> Unit
    ) {
        if (token.isNullOrEmpty()) {
            onError(TelegramException(TELEGRAM_NO_TOKEN, "Token is null"))
            return
        }

        log.debug("Requesting <$command>")

        val request = JsonObjectRequest(
            buildUrl(command, params),
            { response ->
                try {
                    log.debug("Received response <$command>")

                    onResponse(response)
                } catch (x: TelegramException) {
                    log.error("Error processing response <$command>", x)

                    onError(x)
                }
            },
            { error ->
                log.error("Error sending <$command>", error)

                onError(
                    TelegramException(
                        when (error) {
                            is AuthFailureError -> TELEGRAM_INVALID_TOKEN
                            is NetworkError,
                            is TimeoutError -> TELEGRAM_NO_CONNECTION

                            else -> TELEGRAM_REQUEST_FAILED
                        }, error.localizedMessage.orEmpty(), error
                    )
                )
            }
        )

        requestQueue.add(request)
    }

    private fun buildUrl(command: String, params: Map<String, Any>) = buildString {
        append(BASE_URL)
        append(token)
        append("/")
        append(command)

        params.entries.joinTo(
            this,
            prefix = "?",
            separator = "&",
            transform = { "${it.key}=${it.value}" }
        )
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

        private val log = Logger("TelegramSession")

        private val NEW_LINE = URLEncoder.encode("\n", "UTF-8")
        private const val BASE_URL = "https://api.telegram.org/bot"
        private const val PARSE_MODE = "html"
    }

}
