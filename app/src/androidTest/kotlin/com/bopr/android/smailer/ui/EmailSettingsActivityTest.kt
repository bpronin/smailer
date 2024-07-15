package com.bopr.android.smailer.ui


import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_CONTENT
import com.bopr.android.smailer.util.DEVICE_NAME
import org.junit.Test


class EmailSettingsActivityTest : BaseActivityTest(EmailSettingsActivity::class) {

    override fun beforeActivityCreate() {
        settings.update {
            putStringSet(PREF_EMAIL_CONTENT, emptySet())
        }
    }

    @Test
    fun testActivity() {
        testContentPreference()
        testLanguagePreference()
        testDeviceNamePreference()
    }

    private fun testLanguagePreference() {
        testLanguagePreferenceEnglish()
        testLanguagePreferenceCancel()
        testLanguagePreferenceDefault()
    }

    private fun testLanguagePreferenceEnglish() {
        val titles = stringArray(R.array.language_names)

        clickPreference(R.string.email_message_language)
        assertAlertDialogDisplayed(R.string.email_language)

        clickCheckbox(titles[1])

        assertHomeDisplayed()
        assertPreferenceSummaryIs(R.string.email_message_language, titles[1])
    }

    private fun testLanguagePreferenceCancel() {
        val titles = stringArray(R.array.language_names)

        clickPreference(R.string.email_message_language)
        assertAlertDialogDisplayed(R.string.email_language)

        clickCancelButton()
        assertHomeDisplayed()
        assertPreferenceSummaryIs(R.string.email_message_language, titles[1])
    }

    private fun testLanguagePreferenceDefault() {
        val titles = stringArray(R.array.language_names)

        clickPreference(R.string.email_message_language)
        assertAlertDialogDisplayed(R.string.email_language)

        clickCheckbox(titles[0])

        assertHomeDisplayed()
        assertPreferenceSummaryIs(R.string.email_message_language, titles[0])
    }

    private fun testContentPreference() {
        clickPreference(R.string.email_content)
        assertAlertDialogDisplayed(R.string.email_message_content)

        clickCancelButton()
        assertHomeDisplayed()
    }

    private fun testDeviceNamePreference() {
        testDeviceNamePreferenceInput()
        testDeviceNamePreferenceCancel()
        testDeviceNamePreferenceClear()
    }

    private fun testDeviceNamePreferenceCancel() {
        clickPreference(R.string.device_name)

        assertAlertDialogDisplayed(R.string.device_name)

        clickCancelButton()

        assertHomeDisplayed()
        assertPreferenceSummaryIs(R.string.device_name, "device")
    }

    private fun testDeviceNamePreferenceInput() {
        clickPreference(R.string.device_name)

        assertAlertDialogDisplayed(R.string.device_name)

        inputText("device")
        clickOkButton()

        assertHomeDisplayed()
        assertPreferenceSummaryIs(R.string.device_name, "device")
    }

    private fun testDeviceNamePreferenceClear() {
        clickPreference(R.string.device_name)

        assertAlertDialogDisplayed(R.string.device_name)

        clearInputText()
        clickOkButton()

        assertHomeDisplayed()
        assertPreferenceSummaryIs(R.string.device_name, DEVICE_NAME)
    }

    private fun assertHomeDisplayed() {
        assertPageDisplayed(R.string.email_message)
    }

}
