package com.bopr.android.smailer.remote

import android.content.Context
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.Notifications
import com.bopr.android.smailer.Notifications.Companion.TARGET_MAIN
import com.bopr.android.smailer.Notifications.Companion.TARGET_PHONE_BLACKLIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_PHONE_WHITELIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_TEXT_BLACKLIST
import com.bopr.android.smailer.Notifications.Companion.TARGET_TEXT_WHITELIST
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_NOTIFICATIONS
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_PHONE_TO_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_PHONE_TO_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_TEXT_TO_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_TEXT_TO_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_PHONE_FROM_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_PHONE_FROM_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_TEXT_FROM_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_TEXT_FROM_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.SEND_SMS_TO_CALLER
import com.bopr.android.smailer.util.SmsTransport
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@SmallTest
class RemoteControlProcessorTest : BaseTest() {

    private lateinit var context: Context
    private lateinit var settings: Settings
    private lateinit var notifications: Notifications
    private lateinit var smsTransport: SmsTransport

    @Before
    fun setUp() {
        settings = Settings(targetContext, "test.preferences")
        settings.update {
            clear()
            putBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, true)
        }

        notifications = mock()
        smsTransport = mock()
        context = mock {
            on { resources }.doReturn(targetContext.resources)
        }
    }

    @Test
    fun testAddPhoneToBlacklist() {
        val processor = RemoteControlProcessor(context, settings, notifications)

        processor.performTask(RemoteControlTask("device", ADD_PHONE_TO_BLACKLIST).apply { argument = "100" })
        processor.performTask(RemoteControlTask("device", ADD_PHONE_TO_BLACKLIST).apply { argument = "200" })
        processor.performTask(RemoteControlTask("device", ADD_PHONE_TO_BLACKLIST).apply { argument = "+2-00" }) /* should be ignored */

        assertEquals("100,200", settings.getString(PREF_FILTER_PHONE_BLACKLIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.phone_remotely_added_to_blacklist, "100")), eq(TARGET_PHONE_BLACKLIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.phone_remotely_added_to_blacklist, "200")), eq(TARGET_PHONE_BLACKLIST))
    }

    @Test
    fun testAddPhoneToWhitelist() {
        val processor = RemoteControlProcessor(context, settings, notifications)

        processor.performTask(RemoteControlTask("device", ADD_PHONE_TO_WHITELIST).apply { argument = "100" })
        processor.performTask(RemoteControlTask("device", ADD_PHONE_TO_WHITELIST).apply { argument = "200" })
        processor.performTask(RemoteControlTask("device", ADD_PHONE_TO_WHITELIST).apply { argument = "+2-00" }) /* should be ignored */

        assertEquals("100,200", settings.getString(PREF_FILTER_PHONE_WHITELIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.phone_remotely_added_to_whitelist, "100")), eq(TARGET_PHONE_WHITELIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.phone_remotely_added_to_whitelist, "200")), eq(TARGET_PHONE_WHITELIST))
    }

    @Test
    fun testAddTextToBlacklist() {
        val processor = RemoteControlProcessor(context, settings, notifications)

        processor.performTask(RemoteControlTask("device", ADD_TEXT_TO_BLACKLIST).apply { argument = "100" })
        processor.performTask(RemoteControlTask("device", ADD_TEXT_TO_BLACKLIST).apply { argument = "200" })
        processor.performTask(RemoteControlTask("device", ADD_TEXT_TO_BLACKLIST).apply { argument = "200" }) /* should be ignored */

        assertEquals("100,200", settings.getString(PREF_FILTER_TEXT_BLACKLIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.text_remotely_added_to_blacklist, "100")), eq(TARGET_TEXT_BLACKLIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.text_remotely_added_to_blacklist, "200")), eq(TARGET_TEXT_BLACKLIST))
    }

    @Test
    fun testAddTextToWhitelist() {
        val processor = RemoteControlProcessor(context, settings, notifications)

        processor.performTask(RemoteControlTask("device", ADD_TEXT_TO_WHITELIST).apply { argument = "100" })
        processor.performTask(RemoteControlTask("device", ADD_TEXT_TO_WHITELIST).apply { argument = "200" })
        processor.performTask(RemoteControlTask("device", ADD_TEXT_TO_WHITELIST).apply { argument = "200" }) /* should be ignored */

        assertEquals("100,200", settings.getString(PREF_FILTER_TEXT_WHITELIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.text_remotely_added_to_whitelist, "100")), eq(TARGET_TEXT_WHITELIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.text_remotely_added_to_whitelist, "200")), eq(TARGET_TEXT_WHITELIST))
    }

    @Test
    fun testRemovePhoneFromBlacklist() {
        settings.update {
            putStringList(PREF_FILTER_PHONE_BLACKLIST, setOf("100", "200", "300"))
        }

        val processor = RemoteControlProcessor(context, settings, notifications)

        processor.performTask(RemoteControlTask("device", REMOVE_PHONE_FROM_BLACKLIST).apply { argument = "100" })
        processor.performTask(RemoteControlTask("device", REMOVE_PHONE_FROM_BLACKLIST).apply { argument = "+2-00" })

        assertEquals("300", settings.getString(PREF_FILTER_PHONE_BLACKLIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.phone_remotely_removed_from_blacklist, "100")), eq(TARGET_PHONE_BLACKLIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.phone_remotely_removed_from_blacklist, "+2-00")), eq(TARGET_PHONE_BLACKLIST))
    }

    @Test
    fun testRemovePhoneFromWhitelist() {
        settings.update {
            putStringList(PREF_FILTER_PHONE_WHITELIST, setOf("100", "200", "300"))
        }

        val processor = RemoteControlProcessor(context, settings, notifications)

        processor.performTask(RemoteControlTask("device", REMOVE_PHONE_FROM_WHITELIST).apply { argument = "100" })
        processor.performTask(RemoteControlTask("device", REMOVE_PHONE_FROM_WHITELIST).apply { argument = "+2-00" })

        assertEquals("300", settings.getString(PREF_FILTER_PHONE_WHITELIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.phone_remotely_removed_from_whitelist, "100")), eq(TARGET_PHONE_WHITELIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.phone_remotely_removed_from_whitelist, "+2-00")), eq(TARGET_PHONE_WHITELIST))
    }

    @Test
    fun testRemoveTextFromBlacklist() {
        settings.update {
            putStringList(PREF_FILTER_TEXT_BLACKLIST, setOf("100", "200", "300"))
        }

        val processor = RemoteControlProcessor(context, settings, notifications)

        processor.performTask(RemoteControlTask("device", REMOVE_TEXT_FROM_BLACKLIST).apply { argument = "100" })
        processor.performTask(RemoteControlTask("device", REMOVE_TEXT_FROM_BLACKLIST).apply { argument = "200" })

        assertEquals("300", settings.getString(PREF_FILTER_TEXT_BLACKLIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.text_remotely_removed_from_blacklist, "100")), eq(TARGET_TEXT_BLACKLIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.text_remotely_removed_from_blacklist, "200")), eq(TARGET_TEXT_BLACKLIST))
    }

    @Test
    fun testRemoveTextFromWhitelist() {
        settings.update {
            putStringList(PREF_FILTER_TEXT_WHITELIST, setOf("100", "200", "300"))
        }

        val processor = RemoteControlProcessor(context, settings, notifications)

        processor.performTask(RemoteControlTask("device", REMOVE_TEXT_FROM_WHITELIST).apply { argument = "100" })
        processor.performTask(RemoteControlTask("device", REMOVE_TEXT_FROM_WHITELIST).apply { argument = "200" })

        assertEquals("300", settings.getString(PREF_FILTER_TEXT_WHITELIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.text_remotely_removed_from_whitelist, "100")), eq(TARGET_TEXT_WHITELIST))
        verify(notifications).showRemoteAction(
                eq(targetContext.getString(R.string.text_remotely_removed_from_whitelist, "200")), eq(TARGET_TEXT_WHITELIST))
    }

    @Test
    fun testSendSms() {
        val processor = RemoteControlProcessor(context, settings, notifications, smsTransport)

        processor.performTask(RemoteControlTask("device", SEND_SMS_TO_CALLER).apply {
            arguments["phone"] = "100"
            arguments["text"] = "Text"
        })

        verify(smsTransport).sendMessage(eq("100"), eq("Text"))
        verify(notifications).showRemoteAction(eq(targetContext.getString(R.string.sent_sms, "100")), eq(TARGET_MAIN))
    }

}