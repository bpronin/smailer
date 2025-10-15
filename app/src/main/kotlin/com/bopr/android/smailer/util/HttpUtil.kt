package com.bopr.android.smailer.util

import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.HttpsURLConnection.HTTP_OK

fun String.httpEncoded(): String = URLEncoder.encode(this, "UTF-8")

fun Appendable.appendAsHttpParams(map: Map<*, *>) = map.entries.joinTo(
    this,
    prefix = "?",
    separator = "&",
    transform = { "${it.key}=${it.value}" }
)

fun URL.getJsonContent() =
    JSONObject((openConnection() as HttpsURLConnection).run {
        requestMethod = "GET"
        setRequestProperty("Accept", "application/json")
        if (responseCode == HTTP_OK) inputStream else errorStream
    }.readText())