package com.bopr.android.smailer.ui


import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_REMOTE_CONTROL_NOTIFICATIONS
import org.junit.Test


class EmailRemoteControlActivityTest : BaseActivityTest(EmailRemoteControlActivity::class) {

    override fun beforeActivityCreate() {
        settings.update {
            putBoolean(PREF_EMAIL_REMOTE_CONTROL_ENABLED, false)
            putBoolean(PREF_EMAIL_REMOTE_CONTROL_NOTIFICATIONS, false)
            putBoolean(PREF_EMAIL_REMOTE_CONTROL_FILTER_RECIPIENTS, false)
        }
    }

    @Test
    fun testActivity() {
        assertPreferenceDisabled(R.string.email_remote_control_account)
        assertPreferenceDisabled(R.string.notify_remote_actions)
        assertPreferenceDisabled(R.string.allow_only_recipients)
        assertPreferenceDisabled(R.string.process_service_mail)

        testEnablePreference()
    }

    private fun testEnablePreference() {
        clickPreference(R.string.allow_control_remotely)

        assertPreferenceEnabled(R.string.email_remote_control_account)
        assertPreferenceEnabled(R.string.notify_remote_actions)
        assertPreferenceEnabled(R.string.allow_only_recipients)
        assertPreferenceEnabled(R.string.process_service_mail)
    }
}
