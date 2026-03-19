package com.bopr.android.smailer.messenger.telegram

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

@SmallTest
class TelegramMessengerTest : BaseTest() {

    @Test
    fun testTelegramClient(): Unit = runBlocking {
        val client = TelegramClient(getString(R.string.debug_telegram_token))
        val chatId = client.send("This is a test message", null)

        assertNotNull(chatId)
    }
    
    @Test
    fun testPocketbaseMessenger(): Unit = runBlocking {
        targetContext.settings.update {
            putBoolean(PREF_TELEGRAM_MESSENGER_ENABLED, true)
            putString(PREF_TELEGRAM_BOT_TOKEN, getString(R.string.debug_telegram_token))
        }

        val messenger = TelegramMessenger(targetContext)

        assertTrue(messenger.initialize())

        messenger.send(
            Event(
                payload = PhoneCallData(
                    startTime = 0,
                    phone = "123456789",
                    isIncoming = true,
                    text = "Test text"
                )
            )
        )
    }
}