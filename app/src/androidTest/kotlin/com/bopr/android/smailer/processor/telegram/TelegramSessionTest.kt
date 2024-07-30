package com.bopr.android.smailer.processor.telegram

import android.util.Log
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import org.junit.Test
import java.util.concurrent.CountDownLatch

@SmallTest
class TelegramSessionTest : BaseTest() {

    private val TAG = "TelegramSessionTest"

    @Test
    fun sendMessage() {
        val latch = CountDownLatch(1)

        TelegramSession(
            context = targetContext,
            token = "6736609275:AAGYlecb8G_9iJVyOG6Btau3vLrQ_ddJ7NY2"
        ).sendMessage(
            oldChatId = null,
            message = "Test telegram session",
            onSuccess = { chatId ->
                Log.i(TAG, "sendMessage: $chatId")
                latch.countDown()

            },
            onError = { error ->
                Log.e(TAG, "sendMessage", error)
                latch.countDown()
            }
        )

        latch.await()
    }
}