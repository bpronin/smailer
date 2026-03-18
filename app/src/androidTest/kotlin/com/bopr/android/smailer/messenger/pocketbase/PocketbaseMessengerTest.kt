package com.bopr.android.smailer.messenger.pocketbase

import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.util.GeoLocation
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.runBlocking
import org.junit.Test

class PocketbaseMessengerTest : BaseTest() {

    @Test
    fun testInsertEvent(): Unit = runBlocking {
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

        client.insertEvent(event)
    }

//    @Test
//    fun testInsertTelephony(): Unit = runBlocking {
//        val client = PocketbaseClient("http://185.225.202.228:8090")
//
//        client.auth("boris.i.pronin@gmail.com", "blue88cofe", true)
//
//        val data = PhoneCallData(
//            startTime = 0,
//            phone = "123456789",
//            isIncoming = true,
//            text = "Test text"
//        )
//
//        client.insertIntoTelephony("test_id", data)
//    }

    @Test
    fun testSend() = runBlocking {
        val messenger = PocketbaseMessenger(targetContext)
        messenger.prepare()

        val event = Event(
            payload = PhoneCallData(
                startTime = 0,
                phone = "123456789",
                isIncoming = true,
                text = "Test text"
            )
        )

        var failed = false
        messenger.send(
            event,
            onSuccess = {
                println("Success")
            },
            onError = {
                failed = true
            }
        )

        assertFalse(failed)
    }

}