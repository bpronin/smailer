package com.bopr.android.smailer.sync

import android.Manifest.permission.READ_CONTACTS
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.util.primaryAccount
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SyncManagerTest : BaseTest() {

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
        val sync = Synchronizer(targetContext, account, database, "test-meta.json", "test-data.json")

        sync.clear()

        database.phoneBlacklist = listOf("A","B","C")

        sync.sync()

        database.phoneBlacklist = emptyList()
        database.updateTime = 0  /* before last sync to force download */

        sync.sync()

        assertEquals(listOf("A", "B", "C"), database.phoneBlacklist)
    }

}