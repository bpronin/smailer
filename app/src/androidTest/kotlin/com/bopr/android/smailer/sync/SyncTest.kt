package com.bopr.android.smailer.sync

import android.Manifest.permission.READ_CONTACTS
import android.accounts.Account
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.AccountHelper.Companion.accounts
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.data.Database.Companion.databaseName
import com.bopr.android.smailer.external.GoogleDrive
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_FORCE_UPLOAD
import com.nhaarman.mockitokotlin2.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SyncTest : BaseTest() {

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(READ_CONTACTS)

    private lateinit var database: Database
    private lateinit var account: Account

    @Before
    fun setup() {
        account = targetContext.accounts.requirePrimaryGoogleAccount()
        databaseName = "test.sqlite"
        targetContext.deleteDatabase(databaseName)
        database = targetContext.database
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testUpload() {
        val drive = mock<GoogleDrive> {}
        val sync = Synchronizer(targetContext, account, database, drive)

        database.commit {
            phoneBlacklist.addAll(setOf("PBA", "PBB", "PBC"))
            phoneWhitelist.addAll(setOf("PWA", "PWB", "PWC"))
            textBlacklist.addAll(setOf("TBA", "TBB", "TBC"))
            textWhitelist.addAll(setOf("TWA", "TWB", "TWC"))
        }

        sync.sync(SYNC_FORCE_UPLOAD)

        verify(drive).upload(eq("meta.json"), argThat {
            (this as SyncMetaData).run {
                time == database.updateTime
            }
        })
        verify(drive).upload(eq("data.json"), argThat {
            (this as SyncData).run {
                phoneBlacklist == database.phoneBlacklist
                        && phoneWhitelist == database.phoneWhitelist
                        && textBlacklist == database.textBlacklist
                        && textWhitelist == database.textWhitelist
            }
        })

    }

    @Test
    fun testSync() {
        val sync = Synchronizer(targetContext, account, database)

        sync.clear()

        database.commit { phoneBlacklist.addAll(setOf("A", "B", "C")) }

        assertEquals(setOf("A", "B", "C"), database.phoneBlacklist)
        assertFalse(database.updateTime == 0L)

        sync.sync()

        database.commit { phoneBlacklist.clear() }

        assertTrue(database.phoneBlacklist.isEmpty())

        database.updateTime = 0  /* before last sync to force download */

        sync.sync()

        assertEquals(setOf("A", "B", "C"), database.phoneBlacklist)
    }

}