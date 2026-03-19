package com.bopr.android.smailer.messenger.telegram

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.R
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Test

@SmallTest
class TelegramClientTest : BaseTest() {

    @Test
    fun testSendMessage(): Unit = runBlocking {
        val client = TelegramClient(getString(R.string.debug_telegram_token))
        val chatId = client.send("This is a test message", null)

        assertNotNull(chatId)
    }

}