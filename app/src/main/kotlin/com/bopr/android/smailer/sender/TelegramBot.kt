package com.bopr.android.smailer.sender

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bopr.android.smailer.Settings
import org.json.JSONObject
import org.slf4j.LoggerFactory

internal class TelegramBot(context: Context) : MessengerTransport(context) {

    private val log = LoggerFactory.getLogger("TelegramBot")

    private var settings = Settings(context)
    private val baseUrl = "https://api.telegram.org/bot"
//    private var token = "6736609275:AAGYlecb8G_9iJVyOG6Btau3vLrQ_ddJ7NY"
//    private val baseUrl = "https://api.telegram.org/bot$token"

    private val requestQueue = Volley.newRequestQueue(context)

    override fun sendMessages(vararg messages: EventMessage) {
        requestUpdates { chatId ->
            messages.forEach { requestSendMessage(chatId, it.text) }
        }
    }

    private fun request(command: String, onResponse: Response.Listener<JSONObject>) {
        val token =
            requireNotNull(settings.telegramBotToken) { "Telegram bot token is not specified." }
        val url = "$baseUrl$token/$command"
        val request = JsonObjectRequest(url, onResponse) { error ->
            log.error("Request failed", error)  //todo: notify user
        }
        requestQueue.add(request)
    }

    private fun requestUpdates(onResponse: (chatId: String) -> Unit) {
        request("getUpdates") { response ->
            val updates = response.getJSONArray("result")
            if (updates.length() == 0) {
                throw RuntimeException("Start conversation with the bot in Telegram application then try again.")
            }

            val chatId = updates.getJSONObject(0)
                .getJSONObject("message")
                .getJSONObject("chat")
                .getString("id")

            onResponse(chatId)
        }
    }

    private fun requestSendMessage(chatId: String, text: String) {
        request("sendMessage?chat_id=$chatId&text=$text") { response ->
            if (!response.getBoolean("ok")) {
                log.warn("<sendMessage> request unsuccessful.")
            }
        }
    }

}
