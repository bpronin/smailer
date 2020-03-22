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
    var activityTestRule = ActivityTestRule(RulesActivity::class.java)

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
        testTriggersPreference()
        testPhoneBlacklistPreference()
        testPhoneWhitelistPreference()
        testTextBlacklistPreference()
        testTextWhitelistPreference()
    }

    private fun testTextWhitelistPreference() {
        clickPreferenceAtPosition(6)
        assertPageDisplayed(R.string.text_whitelist)

        clickBackButton()
        assertHomeDisplayed()
    }

    private fun testTextBlacklistPreference() {
        clickPreferenceAtPosition(5)
        assertPageDisplayed(R.string.text_blacklist)

        clickBackButton()
        assertHomeDisplayed()
    }

    private fun testPhoneWhitelistPreference() {
        clickPreferenceAtPosition(3)
        assertPageDisplayed(R.string.phone_whitelist)

        clickBackButton()
        assertHomeDisplayed()
    }

    private fun testPhoneBlacklistPreference() {
        clickPreferenceAtPosition(2)
        assertPageDisplayed(R.string.phone_blacklist)

        clickBackButton()
        assertHomeDisplayed()
    }

    private fun testTriggersPreference() {
        clickPreference(R.string.triggers)
        assertAlertDialogDisplayed(R.string.triggers)

        clickCancelButton()
        assertHomeDisplayed()
    }

    private fun assertHomeDisplayed() {
        assertPageDisplayed(R.string.rules_for_sending)
    }
}
