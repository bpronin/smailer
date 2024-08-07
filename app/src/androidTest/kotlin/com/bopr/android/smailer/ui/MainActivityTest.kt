package com.bopr.android.smailer.ui


import com.bopr.android.smailer.R
import org.junit.Test


class MainActivityTest : BaseActivityTest(MainActivity::class) {

    override fun beforeActivityCreate() {
        settings.update {
            /* hide battery optimization dialog */
            putBoolean("battery-optimization-do-not-ask-again", true)
        }
    }

    @Test
    fun testActivity() {
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

        clickPreference(R.string.history)
        assertPageDisplayed(R.string.history)

        clickBackButton()
        assertHomeDisplayed()

        clickPreference(R.string.more_options)
        assertPageDisplayed(R.string.more_options)

        clickBackButton()
        assertHomeDisplayed()

        clickOptionsMenuItem(R.string.about)
        assertAlertDialogDisplayed(R.string.app_title)
    }

    private fun assertHomeDisplayed() {
        assertPageDisplayed(R.string.app_name)
    }

}
