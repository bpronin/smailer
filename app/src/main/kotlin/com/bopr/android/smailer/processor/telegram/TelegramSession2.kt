package com.bopr.android.smailer.processor.telegram

import android.content.Context
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_NO_UPDATES
import com.bopr.android.smailer.processor.telegram.TelegramException.Code.TELEGRAM_REQUEST_FAILED
import com.bopr.android.smailer.util.getJsonContent
import org.json.JSONObject
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

/**
 * Telegram bot wrapper.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramSession2(context: Context, private val token: String?) {

    private val executor by lazyOf(Executors.newSingleThreadExecutor())

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
            return
        }

        executor.execute { requestSync(command, onResponse, onError) }
    }

    private fun requestSync(
        command: String,
        onResponse: (response: JSONObject) -> Unit,
        onError: (error: TelegramException) -> Unit
    ) {
        val url = URL("$BASE_URL$token/$command")
        runCatching { url.getJsonContent() }
            .onSuccess {response->
                try {
                    onResponse(response)
                } catch (x: TelegramException) {
                    log.error("Error while processing <$command> response.", x)

                    onError(x)
                }
            }
            .onFailure { error->
                log.error("Error while sending <$command> request.", error)

                onError(
                    TelegramException(
                        TELEGRAM_REQUEST_FAILED,
                        "Error while sending <$command> request.",
                        error
                    )
                )
            }

//        try {
//            val response = url.getJsonContent()
//
//            try {
//                onResponse(response)
//            } catch (x: TelegramException) {
//                log.error("Error while processing <$command> response.", x)
//
//                onError(x)
//            }
//        } catch (x: Exception) {
//            log.error("Error while sending <$command> request.", x)
//
//            onError(
//                TelegramException(
//                    TELEGRAM_REQUEST_FAILED,
//                    "Error while sending <$command> request.",
//                    x
//                )
//            )
//        }
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
