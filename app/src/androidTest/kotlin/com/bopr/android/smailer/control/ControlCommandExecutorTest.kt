package com.bopr.android.smailer.control

import android.content.Context
import android.content.SharedPreferences
import androidx.test.filters.SmallTest
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_NOTIFICATIONS
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_PHONE_TO_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_PHONE_TO_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_TEXT_TO_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_TEXT_TO_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_PHONE_FROM_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_PHONE_FROM_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_TEXT_FROM_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_TEXT_FROM_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.SEND_SMS_TO_CALLER
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.data.Database.Companion.databaseName
import com.bopr.android.smailer.ui.PhoneBlacklistFilterActivity
import com.bopr.android.smailer.ui.PhoneWhitelistFilterActivity
import com.bopr.android.smailer.ui.TextWhitelistFilterActivity
import com.bopr.android.smailer.ui.MainActivity
import com.bopr.android.smailer.util.sendSmsMessage
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString

@SmallTest
class ControlCommandExecutorTest : BaseTest() {

    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private lateinit var database: Database
    private lateinit var notifications: NotificationsHelper

    @Before
    fun setUp() {
        preferences = mock {
            on {
                getBoolean(eq(PREF_REMOTE_CONTROL_NOTIFICATIONS), anyOrNull())
            }.doReturn(true)
        }

        databaseName = "test.sqlite"
        targetContext.deleteDatabase(databaseName)
        database = targetContext.database

        notifications = mock()

        context = mock {
            on {
                resources
            }.doReturn(targetContext.resources)
            on {
                getSharedPreferences(anyString(), anyInt())
            }.doReturn(preferences)
        }
    }

    fun tearDown() {
        database.close()
    }

    private fun createCommand(
        action: ControlCommand.Action,
        argument: String
    ): ControlCommand {
        return ControlCommand("device").apply {
            this.action = action
            this.argument = argument
        }
    }

    @Test
    fun testAddPhoneToBlacklist() {
        val processor = ControlCommandExecutor(context, database, notifications = notifications)

        processor.execute(createCommand(ADD_PHONE_TO_BLACKLIST, "100"))
        processor.execute(createCommand(ADD_PHONE_TO_BLACKLIST, "200"))
        processor.execute(
            createCommand(ADD_PHONE_TO_BLACKLIST, "200")
        ) /* must be ignored */

        assertEquals(setOf("100", "200"), database.phoneBlacklist)
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.phone_remotely_added_to_blacklist, "100")),
            eq(PhoneBlacklistFilterActivity::class)
        )
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.phone_remotely_added_to_blacklist, "200")),
            eq(PhoneBlacklistFilterActivity::class)
        )
    }

    @Test
    fun testAddPhoneToWhitelist() {
        val processor = ControlCommandExecutor(context, database, notifications = notifications)

        processor.execute(createCommand(ADD_PHONE_TO_WHITELIST, "100"))
        processor.execute(createCommand(ADD_PHONE_TO_WHITELIST, "200"))
        processor.execute(
            createCommand(
                ADD_PHONE_TO_WHITELIST,
                "200"
            )
        ) /* must be ignored */

        assertEquals(setOf("100", "200"), database.phoneWhitelist)
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.phone_remotely_added_to_whitelist, "100")),
            eq(PhoneWhitelistFilterActivity::class)
        )
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.phone_remotely_added_to_whitelist, "200")),
            eq(PhoneWhitelistFilterActivity::class)
        )
    }

    @Test
    fun testAddTextToBlacklist() {
        val processor = ControlCommandExecutor(context, database, notifications = notifications)

        processor.execute(createCommand(ADD_TEXT_TO_BLACKLIST, "100"))
        processor.execute(createCommand(ADD_TEXT_TO_BLACKLIST, "200"))
        processor.execute(createCommand(ADD_TEXT_TO_BLACKLIST, "200"))
        /* must be ignored */

        assertEquals(setOf("100", "200"), database.textBlacklist)
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.text_remotely_added_to_blacklist, "100")),
            eq(PhoneBlacklistFilterActivity::class)
        )
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.text_remotely_added_to_blacklist, "200")),
            eq(PhoneBlacklistFilterActivity::class)
        )
    }

    @Test
    fun testAddTextToWhitelist() {
        val processor = ControlCommandExecutor(context, database, notifications = notifications)

        processor.execute(createCommand(ADD_TEXT_TO_WHITELIST, "100"))
        processor.execute(createCommand(ADD_TEXT_TO_WHITELIST, "200"))
        processor.execute(createCommand(ADD_TEXT_TO_WHITELIST, "200"))

        assertEquals(setOf("100", "200"), database.textWhitelist)
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.text_remotely_added_to_whitelist, "100")),
            eq(TextWhitelistFilterActivity::class)
        )
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.text_remotely_added_to_whitelist, "200")),
            eq(TextWhitelistFilterActivity::class)
        )
    }

    @Test
    fun testRemovePhoneFromBlacklist() {
        database.phoneBlacklist.replaceAll(setOf("100", "200", "300"))

        val processor = ControlCommandExecutor(context, database, notifications = notifications)

        processor.execute(createCommand(REMOVE_PHONE_FROM_BLACKLIST, "100"))
        processor.execute(createCommand(REMOVE_PHONE_FROM_BLACKLIST, "200"))

        assertEquals(setOf("300"), database.phoneBlacklist)
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.phone_remotely_removed_from_blacklist, "100")),
            eq(PhoneBlacklistFilterActivity::class)
        )
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.phone_remotely_removed_from_blacklist, "200")),
            eq(PhoneBlacklistFilterActivity::class)
        )
    }

    @Test
    fun testRemovePhoneFromWhitelist() {
        database.phoneWhitelist.replaceAll(setOf("100", "200", "300"))

        val processor = ControlCommandExecutor(context, database, notifications = notifications)

        processor.execute(createCommand(REMOVE_PHONE_FROM_WHITELIST, "100"))
        processor.execute(createCommand(REMOVE_PHONE_FROM_WHITELIST, "200"))

        assertEquals(setOf("300"), database.phoneWhitelist)
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.phone_remotely_removed_from_whitelist, "100")),
            eq(PhoneWhitelistFilterActivity::class)
        )
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.phone_remotely_removed_from_whitelist, "200")),
            eq(PhoneWhitelistFilterActivity::class)
        )
    }

    @Test
    fun testRemoveTextFromBlacklist() {
        database.textBlacklist.replaceAll(setOf("100", "200", "300"))

        val processor = ControlCommandExecutor(context, database, notifications = notifications)

        processor.execute(createCommand(REMOVE_TEXT_FROM_BLACKLIST, "100"))
        processor.execute(createCommand(REMOVE_TEXT_FROM_BLACKLIST, "200"))

        assertEquals(setOf("300"), database.textBlacklist)
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.text_remotely_removed_from_blacklist, "100")),
            eq(PhoneBlacklistFilterActivity::class)
        )
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.text_remotely_removed_from_blacklist, "200")),
            eq(PhoneBlacklistFilterActivity::class)
        )
    }

    @Test
    fun testRemoveTextFromWhitelist() {
        database.textWhitelist.replaceAll(setOf("100", "200", "300"))

        val processor = ControlCommandExecutor(context, database, notifications = notifications)

        processor.execute(createCommand(REMOVE_TEXT_FROM_WHITELIST, "100"))
        processor.execute(createCommand(REMOVE_TEXT_FROM_WHITELIST, "200"))

        assertEquals(setOf("300"), database.textWhitelist)
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.text_remotely_removed_from_whitelist, "100")),
            eq(TextWhitelistFilterActivity::class)
        )
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.text_remotely_removed_from_whitelist, "200")),
            eq(TextWhitelistFilterActivity::class)
        )
    }

    @Test
    fun testSendSms() {
        val processor = ControlCommandExecutor(
            context,
            database,
            notifications = notifications,
        )

        processor.execute(ControlCommand("device").apply {
            action = SEND_SMS_TO_CALLER
            arguments["phone"] = "100"
            arguments["text"] = "Text"
        })

        verify(context).sendSmsMessage(eq("100"), eq("Text"))
        verify(notifications).notifyInfo(
            eq(getString(R.string.remote_control)),
            eq(getString(R.string.sent_sms, "100")),
            eq(MainActivity::class)
        )
    }

}