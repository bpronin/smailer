package com.bopr.android.smailer.messenger.pocketbase

import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_BASE_URL
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_PASSWORD
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_USER
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.GeoLocation
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class PocketbaseMessengerTest : BaseTest() {

    @Test
    fun testInsertEvent() = runBlocking {
        val client = PocketbaseClient("http://193.162.143.66:8090")
        client.auth("boris.i.pronin@gmail.com", "blue88cofe")

        val event = Event(
            time = 0,
            processTime = 0,
            target = "Some device",
            location = GeoLocation(10.0, 20.0),
            payload = PhoneCallData(
                startTime = 0,
                phone = "123456789",
                isIncoming = true,
                text = "Test text"
            )
        )

        val id = client.insertEvent(event)

        assertTrue(id.isNotEmpty())
    }

    @Test
    fun testPocketbaseMessenger(): Unit = runBlocking {
        targetContext.settings.update {
            putBoolean(PREF_POCKETBASE_MESSENGER_ENABLED, true)
            putString(PREF_POCKETBASE_BASE_URL, "http://193.162.143.66:8090")
            putString(PREF_POCKETBASE_USER, "boris.i.pronin@gmail.com")
            putString(PREF_POCKETBASE_PASSWORD, "blue88cofe")
        }

        val messenger = PocketbaseMessenger(targetContext)

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