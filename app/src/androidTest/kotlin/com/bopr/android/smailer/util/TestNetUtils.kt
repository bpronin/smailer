package com.bopr.android.smailer.util

import android.util.Log
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import org.junit.Test
import java.net.URL

@SmallTest
class TestNetUtils : BaseTest() {

    private val TAG = "TelegramSessionTest"

    @Test
    fun sendMessage() {
        val url =
            URL("https://api.telegram.org/bot6736609275:AAGYlecb8G_9iJVyOG6Btau3vLrQ_ddJ7NY2/getChat?chat_id=1901721434")

        val response = url.getJsonContent()
        Log.d(TAG, "sendMessage: $response")
    }
}