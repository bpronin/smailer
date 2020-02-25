package com.bopr.android.smailer.sync

import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.READ_CONTACTS
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_TIME
import com.bopr.android.smailer.ui.GoogleAuthorizationHelper.Companion.primaryAccount
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SyncAdapterTest : BaseTest() {

    @Rule
    @JvmField
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(GET_ACCOUNTS, READ_CONTACTS)

    private lateinit var database: Database

    @Before
    fun setup() {
        database = Database(targetContext, "test.sqlite")
        database.destroy()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testSync() {
        val settings = Settings(targetContext)
        val account = primaryAccount(targetContext)!!
        val sync = Synchronizer(targetContext, account, database, settings, "test-meta.json", "test-data.json")

        sync.clear()

        settings.edit()
                .putLong(PREF_SYNC_TIME, 2)
                .putString(PREF_FILTER_PHONE_BLACKLIST, "A,B,C")
                .apply()
        sync.sync()

        assertEquals(setOf("A", "B", "C"), settings.callFilter.phoneBlacklist)

        settings.edit()
                .putLong(PREF_SYNC_TIME, 1) /* earlier than previous */
                .putString(PREF_FILTER_PHONE_BLACKLIST, "A,B,C,D")
                .apply()
        sync.sync()

        assertEquals(setOf("A", "B", "C"), settings.callFilter.phoneBlacklist)

        settings.edit()
                .putLong(PREF_SYNC_TIME, 3) /* later than previous */
                .putString(PREF_FILTER_PHONE_BLACKLIST, "A,B")
                .apply()
        sync.sync()

        assertEquals(setOf("A", "B"), settings.callFilter.phoneBlacklist)
    }
}