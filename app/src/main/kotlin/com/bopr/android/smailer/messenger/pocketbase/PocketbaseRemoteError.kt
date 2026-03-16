package com.bopr.android.smailer.messenger.pocketbase

import okhttp3.Response
import org.json.JSONObject

class PocketbaseRemoteError(message: String, response: Response) :
    Exception("$message - ${parseResponse(response)}") {

    companion object {
        fun parseResponse(response: Response): String {
            val json = JSONObject(response.body.string())
            return json.getString("message")
        }
    }
}