package com.bopr.android.smailer.sync

import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.READ_CONTACTS
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_TIME
import com.bopr.android.smailer.util.primaryAccount
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SyncAdapterTest : BaseTest() {

    @Rule
    @JvmField
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(GET_ACCOUNTS, READ_CONTACTS)

//    @Test
//    fun testParser(){
//        val stream = InstrumentationRegistry.getInstrumentation().context.assets.open("sync_data.json")
//        val data = JacksonFactory.getDefaultInstance().createJsonParser(stream).parseAndClose(SyncData::class.java)
//
//        assertEquals(SyncData(
//             //...
//        ) , data)
//    }

    @Test
    fun testSync() {
        val context = targetContext
        val database = Database(context, "test.sqlite")
        val settings = Settings(context)
        val account = primaryAccount(context)
        val sync = Synchronizer(context, account, database, settings, "test-meta.json", "test-data.json")

        database.destroy()
        sync.clear()

        settings.edit()
                .putLong(PREF_SYNC_TIME, 2)
                .putString(PREF_FILTER_PHONE_BLACKLIST, "A,B,C")
                .apply()
        sync.sync()

        assertEquals(setOf("A", "B", "C"), settings.getCallFilter().phoneBlacklist)

        settings.edit()
                .putLong(PREF_SYNC_TIME, 1) /* earlier than previous */
                .putString(PREF_FILTER_PHONE_BLACKLIST, "A,B,C,D")
                .apply()
        sync.sync()

        assertEquals(setOf("A", "B", "C"), settings.getCallFilter().phoneBlacklist)

        settings.edit()
                .putLong(PREF_SYNC_TIME, 3) /* later than previous */
                .putString(PREF_FILTER_PHONE_BLACKLIST, "A,B")
                .apply()
        sync.sync()

        assertEquals(setOf("A", "B"), settings.getCallFilter().phoneBlacklist)

        sync.dispose()
    }
}