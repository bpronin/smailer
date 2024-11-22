package com.bopr.android.smailer.ui


import android.app.Activity
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.databaseName
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.Settings.Companion.sharedPreferencesName
import com.bopr.android.smailer.data.Database.Companion.database
import org.junit.After
import org.junit.Rule
import org.junit.runner.RunWith
import kotlin.reflect.KClass


@LargeTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
abstract class BaseActivityTest(private val activityClass: KClass<out Activity>) : BaseTest() {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.RECEIVE_SMS",
                    "android.permission.SEND_SMS",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.READ_CONTACTS",
                    "android.permission.READ_SMS",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.READ_CALL_LOG",
                    "android.permission.READ_PHONE_STATE")


    @get:Rule
    val activityTestRule: ActivityScenarioRule<out Activity>
        get() {
            sharedPreferencesName = "test.preferences"
            databaseName = "test.sqlite"

            targetContext.deleteSharedPreferences(sharedPreferencesName)
            targetContext.deleteDatabase(databaseName)

            settings = targetContext.settings
            database = targetContext.database

            beforeActivityCreate()
            return ActivityScenarioRule(activityClass.java)
        }

    lateinit var settings: Settings
    lateinit var database: Database

    @After
    fun tearDown() {
        database.close()
    }

    protected open fun beforeActivityCreate() {}

}
