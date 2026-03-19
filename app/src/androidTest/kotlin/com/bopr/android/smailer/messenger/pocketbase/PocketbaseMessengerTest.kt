package com.bopr.android.smailer.messenger.pocketbase

import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.R
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
    fun testPocketbaseClient() = runBlocking {
        val client = PocketbaseClient(getString(R.string.debug_pocketbase_url))
        client.auth(
            getString(R.string.debug_pocketbase_user),
            getString(R.string.debug_pocketbase_password)
        )

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
            putString(PREF_POCKETBASE_BASE_URL, getString(R.string.debug_pocketbase_url))
            putString(PREF_POCKETBASE_USER, getString(R.string.debug_pocketbase_user))
            putString(PREF_POCKETBASE_PASSWORD, getString(R.string.debug_pocketbase_password))
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