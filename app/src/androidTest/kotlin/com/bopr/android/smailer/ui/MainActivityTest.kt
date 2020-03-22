package com.bopr.android.smailer.ui


import androidx.test.espresso.Espresso.onView
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(androidx.test.ext.junit.runners.AndroidJUnit4::class)
class MainActivityTest {

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
    fun testMainActivity() {
        if (onView(alertDialog(R.string.battery_optimization)).isExists()) {
            clickCancelButton()
        }
        assertHomeDisplayed()

/*      it won't work cause account picker is outside app process
        clickPreference(R.string.sender)
        checkAccountPicker()
*/

        clickPreference(R.string.recipients)
        assertPageDisplayed(R.string.recipients)

        clickBackButton()
        assertHomeDisplayed()

        clickPreference(R.string.rules)
        assertPageDisplayed(R.string.rules_for_sending)

        clickBackButton()
        assertHomeDisplayed()

        clickPreference(R.string.email_message)
        assertPageDisplayed(R.string.email_message)

        clickBackButton()
        assertHomeDisplayed()

        clickPreference(R.string.remote_control)
        assertPageDisplayed(R.string.remote_control)

        clickBackButton()
        assertHomeDisplayed()

        clickPreference(R.string.call_history)
        assertPageDisplayed(R.string.call_history)

        clickBackButton()
        assertHomeDisplayed()

        clickPreference(R.string.options)
        assertPageDisplayed(R.string.options)

        clickBackButton()
        assertHomeDisplayed()
    }

    private fun assertHomeDisplayed() {
        assertPageDisplayed(R.string.app_name)
    }

}
