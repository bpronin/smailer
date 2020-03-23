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
        clickPreferenceAt(R.string.whitelist, 6)
        assertPageDisplayed(R.string.text_whitelist)

        clickBackButton()
        assertHomeDisplayed()
    }

    private fun testTextBlacklistPreference() {
        clickPreferenceAt(R.string.blacklist, 5)
        assertPageDisplayed(R.string.text_blacklist)

        clickBackButton()
        assertHomeDisplayed()
    }

    private fun testPhoneWhitelistPreference() {
        clickPreferenceAt(R.string.whitelist, 3)
        assertPageDisplayed(R.string.phone_whitelist)

        clickBackButton()
        assertHomeDisplayed()
    }

    private fun testPhoneBlacklistPreference() {
        clickPreferenceAt(R.string.blacklist, 2)
        assertPageDisplayed(R.string.phone_blacklist)

        clickBackButton()
        assertHomeDisplayed()
    }

    @Test
    fun testTriggersPreference() {
        testTriggersPreferenceCheckAll()
        testTriggersPreferenceCancel()
        testTriggersPreferenceUncheckAll()
    }

    private fun testTriggersPreferenceCheckAll() {
        clickPreference(R.string.triggers)
        assertAlertDialogDisplayed(R.string.triggers)

        val titles = stringArray(R.array.trigger_names)
        setCheckboxChecked(titles[0])
        setCheckboxChecked(titles[1])
        setCheckboxChecked(titles[2])
        setCheckboxChecked(titles[3])
        setCheckboxChecked(titles[4])

        clickOkButton()

        assertHomeDisplayed()
        assertPreferenceSummaryEquals(R.string.triggers, R.string.events_causing_sending_mail)
    }

    private fun testTriggersPreferenceUncheckAll() {
        clickPreference(R.string.triggers)
        assertAlertDialogDisplayed(R.string.triggers)

        val titles = stringArray(R.array.trigger_names)
        setCheckboxUnchecked(titles[0])
        setCheckboxUnchecked(titles[1])
        setCheckboxUnchecked(titles[2])
        setCheckboxUnchecked(titles[3])
        setCheckboxUnchecked(titles[4])

        clickOkButton()

        assertHomeDisplayed()
        assertPreferenceSummaryEquals(R.string.triggers, R.string.no_triggers_specified)
    }

    private fun testTriggersPreferenceCancel() {
        clickPreference(R.string.triggers)
        assertAlertDialogDisplayed(R.string.triggers)

        val titles = stringArray(R.array.trigger_names)
        setCheckboxUnchecked(titles[0])
        setCheckboxUnchecked(titles[1])
        setCheckboxUnchecked(titles[2])
        setCheckboxUnchecked(titles[3])
        setCheckboxUnchecked(titles[4])

        clickCancelButton()

        assertHomeDisplayed()
        assertPreferenceSummaryEquals(R.string.triggers, R.string.events_causing_sending_mail)
    }

    private fun assertHomeDisplayed() {
        assertPageDisplayed(R.string.rules_for_sending)
    }
}
