package com.bopr.android.smailer.messenger.telegram

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import org.junit.Test
import java.util.concurrent.CountDownLatch

@SmallTest
class TelegramSessionTest : BaseTest() {

    @Test
    fun sendMessage() {
        val latch = CountDownLatch(1)

        TelegramSession(
            context = targetContext,
            token = "6736609275:AAGYlecb8G_9iJVyOG6Btau3vLrQ_ddJ7NY2"
        ).sendMessage(
            oldChatId = null,
            message = "Test telegram session",
            onSuccess = {
                latch.countDown()

            },
            onError = {
                latch.countDown()
            }
        )

        latch.await()
    }
}