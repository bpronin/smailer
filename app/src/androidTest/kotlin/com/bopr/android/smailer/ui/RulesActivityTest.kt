package com.bopr.android.smailer.ui


import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_PHONE_PROCESS_TRIGGERS
import org.junit.Test


class RulesActivityTest : BaseActivityTest(RulesActivity::class) {

    override fun beforeActivityCreate() {
        settings.update {
            putStringSet(PREF_PHONE_PROCESS_TRIGGERS, emptySet())
        }
    }

    @Test
    fun testActivity() {
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

    private fun testTriggersPreference() {
        testTriggersPreferenceCheckAll()
        testTriggersPreferenceCancel()
        testTriggersPreferenceUncheckAll()
    }

    private fun testTriggersPreferenceCheckAll() {
        val titles = stringArray(R.array.trigger_names)

        clickPreference(R.string.triggers)

        assertAlertDialogDisplayed(R.string.triggers)
        for (title in titles) {
            assertCheckboxUnchecked(title)
        }

        for (title in titles) {
            clickCheckbox(title)
        }
        clickOkButton()

        assertHomeDisplayed()
        assertPreferenceSummaryIs(R.string.triggers, R.string.email_processing_triggers)
    }

    private fun testTriggersPreferenceUncheckAll() {
        val titles = stringArray(R.array.trigger_names)

        clickPreference(R.string.triggers)

        assertAlertDialogDisplayed(R.string.triggers)
        for (title in titles) {
            assertCheckboxChecked(title)
        }

        for (title in titles) {
            clickCheckbox(title)
        }
        clickOkButton()

        assertHomeDisplayed()
        assertPreferenceSummaryIs(R.string.triggers, R.string.no_triggers_specified)
    }

    private fun testTriggersPreferenceCancel() {
        val titles = stringArray(R.array.trigger_names)

        clickPreference(R.string.triggers)
        assertAlertDialogDisplayed(R.string.triggers)

        for (title in titles) {
            clickCheckbox(title)
        }
        clickCancelButton()

        assertHomeDisplayed()
        assertPreferenceSummaryIs(R.string.triggers, R.string.email_processing_triggers)
    }

    private fun assertHomeDisplayed() {
        assertPageDisplayed(R.string.rules_for_sending)
    }
}
