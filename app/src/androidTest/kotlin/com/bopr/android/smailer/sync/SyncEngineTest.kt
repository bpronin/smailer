package com.bopr.android.smailer.sync

import android.Manifest.permission.READ_CONTACTS
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.util.primaryAccount
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SyncEngineTest : BaseTest() {

    @Rule
    @JvmField
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(READ_CONTACTS)

    private lateinit var database: Database

    @Before
    fun setup() {
        database = Database(targetContext, "test.sqlite").apply { clean() }
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testSync() {
        val account = targetContext.primaryAccount()!!
        val settings = Settings(targetContext, "test.preferences")
        val sync = Synchronizer(targetContext, account, database, settings, "test-meta.json", "test-data.json")

        sync.clear()

//        settings.update {
//            putString(PREF_FILTER_PHONE_BLACKLIST, "A,B,C")
//        }
//        sync.sync()
//
//        assertEquals(setOf("A", "B", "C"), settings.callFilter.phoneBlacklist)
//
//        settings.update {
//            putString(PREF_FILTER_PHONE_BLACKLIST, "A,B,C,D")
//        }
//        sync.sync()
//
//        assertEquals(setOf("A", "B", "C"), settings.callFilter.phoneBlacklist)
//
//        settings.update {
//            putString(PREF_FILTER_PHONE_BLACKLIST, "A,B")
//        }
//        sync.sync()
//
//        assertEquals(setOf("A", "B"), settings.callFilter.phoneBlacklist)
    }
}