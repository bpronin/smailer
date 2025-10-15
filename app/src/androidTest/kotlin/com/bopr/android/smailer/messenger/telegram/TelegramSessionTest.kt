package com.bopr.android.smailer.messenger.telegram

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.R
import org.junit.Test
import java.util.concurrent.CountDownLatch

@SmallTest
class TelegramSessionTest : BaseTest() {

    @Test
    fun sendMessage() {
        val latch = CountDownLatch(1)

        TelegramSession(
            context = targetContext,
            token = Companion.getString(R.string.debug_telegram_token)
        ).sendMessage(
            oldChatId = null,
            message = "<b>Bold</b> <i>italic</i> &amp; &lt;#&gt;\nNew line here",
            messageFormat = "HTML",
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