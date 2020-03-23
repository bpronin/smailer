package com.bopr.android.smailer.ui


import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_NOTIFICATIONS
import org.junit.Test


class RemoteControlActivityTest : BaseActivityTest(RemoteControlActivity::class) {

    override fun beforeActivityCreate() {
        settings.update {
            putBoolean(PREF_REMOTE_CONTROL_ENABLED, false)
            putBoolean(PREF_REMOTE_CONTROL_NOTIFICATIONS, false)
            putBoolean(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, false)
        }
    }

    @Test
    fun testActivity() {
        assertPreferenceDisabled(R.string.remote_control_account)
        assertPreferenceDisabled(R.string.notify_remote_actions)
        assertPreferenceDisabled(R.string.allow_only_recipients)
        assertPreferenceDisabled(R.string.process_service_mail)

        testEnablePreference()
    }

    private fun testEnablePreference() {
        clickPreference(R.string.allow_control_remotely)

        assertPreferenceEnabled(R.string.remote_control_account)
        assertPreferenceEnabled(R.string.notify_remote_actions)
        assertPreferenceEnabled(R.string.allow_only_recipients)
        assertPreferenceEnabled(R.string.process_service_mail)
    }
}
