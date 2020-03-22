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
        clickPreference(R.string.email_message_language)
        assertAlertDialogDisplayed(R.string.email_language)

        clickCancelButton()
        assertHomeDisplayed()
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
        assertPreferenceSummaryEquals(R.string.device_name, "device")
    }

    private fun testDeviceNamePreferenceInput() {
        clickPreference(R.string.device_name)

        assertAlertDialogDisplayed(R.string.device_name)

        inputText("device")
        clickOkButton()

        assertHomeDisplayed()
        assertPreferenceSummaryEquals(R.string.device_name, "device")
    }

    private fun testDeviceNamePreferenceClear() {
        clickPreference(R.string.device_name)

        assertAlertDialogDisplayed(R.string.device_name)

        clearInputText()
        clickOkButton()

        assertHomeDisplayed()
        assertPreferenceSummaryEquals(R.string.device_name, deviceName())
    }

    private fun assertHomeDisplayed() {
        assertPageDisplayed(R.string.email_message)
    }

}
