package com.bopr.android.smailer.util

import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.HttpsURLConnection.HTTP_OK

fun URL.getJsonContent(): JSONObject {
    val stream = (openConnection() as HttpsURLConnection).run {
        requestMethod = "GET"
        setRequestProperty("Accept", "application/json")
        if (responseCode == HTTP_OK) inputStream else errorStream
    }
    return JSONObject(stream.readText())
}