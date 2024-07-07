package com.bopr.android.smailer

import androidx.test.filters.SmallTest
import com.bopr.android.smailer.sender.TelegramBot
import com.nhaarman.mockitokotlin2.*
import org.junit.Test

/**
 * [CallProcessor] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class TelegramBotTest : BaseTest() {

    @Test
    fun testProcessMailSent() {
        val transport = TelegramBot(targetContext)
        transport.startSession()
        transport.sendMessages("0", "subject", "aaaa!")
    }
}