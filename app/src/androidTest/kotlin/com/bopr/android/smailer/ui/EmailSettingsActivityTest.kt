package com.bopr.android.smailer.ui


import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.deviceName
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class EmailSettingsActivityTest {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(EmailSettingsActivity::class.java)

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
    fun testEmailSettingsActivity() {
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
        assertPreferenceSummaryIs(R.string.device_name, deviceName())
    }

    private fun assertHomeDisplayed() {
        assertPageDisplayed(R.string.email_message)
    }

}
