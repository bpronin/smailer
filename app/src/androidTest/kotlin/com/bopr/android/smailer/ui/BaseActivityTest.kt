package com.bopr.android.smailer.ui


import android.app.Activity
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.sharedPreferencesName
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
    val activityTestRule: ActivityTestRule<out Activity>
        get() {
            sharedPreferencesName = "test.preferences"
            targetContext.deleteSharedPreferences(sharedPreferencesName)
            settings = Settings(targetContext)

            beforeActivityCreate()
            return ActivityTestRule(activityClass.java)
        }

    lateinit var settings: Settings

    protected open fun beforeActivityCreate() {}

}
