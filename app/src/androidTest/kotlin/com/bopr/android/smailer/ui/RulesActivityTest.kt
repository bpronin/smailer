package com.bopr.android.smailer.ui


import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class RulesActivityTest {

    @Rule
    @JvmField
    var activityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var grantPermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.RECEIVE_SMS",
                    "android.permission.SEND_SMS",
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.READ_CONTACTS",
                    "android.permission.READ_SMS",
                    "android.permission.ACCESS_COARSE_LOCATION",
                    "android.permission.READ_CALL_LOG",
                    "android.permission.READ_PHONE_STATE")

    @Test
    fun testRulesActivity() {
        hideBatteryOptimizationDialog()
        assertPageDisplayed(R.string.app_name)

        clickPreference(R.string.rules)
        assertPageDisplayed(R.string.rules_for_sending)

        clickPreference(R.string.triggers)
        assertAlertDialogDisplayed(R.string.triggers)

        clickCancelButton()
        assertPageDisplayed(R.string.rules_for_sending)

        clickPreferenceAtPosition(2)
        assertPageDisplayed(R.string.phone_blacklist)

        clickBackButton()
        assertPageDisplayed(R.string.rules_for_sending)

        clickPreferenceAtPosition(3)
        assertPageDisplayed(R.string.phone_whitelist)

        clickBackButton()
        assertPageDisplayed(R.string.rules_for_sending)

        clickPreferenceAtPosition(5)
        assertPageDisplayed(R.string.text_blacklist)

        clickBackButton()
        assertPageDisplayed(R.string.rules_for_sending)

        clickPreferenceAtPosition(6)
        assertPageDisplayed(R.string.text_whitelist)

        clickBackButton()
        assertPageDisplayed(R.string.rules_for_sending)
    }
}
