package com.bopr.android.smailer.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import com.bopr.android.smailer.GoogleAuthorizationHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_NOTIFICATIONS
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.enable
import com.bopr.android.smailer.util.TextUtil.isNullOrEmpty
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM

class RemoteControlFragment : BasePreferenceFragment() {

    private lateinit var authorizator: GoogleAuthorizationHelper
    private lateinit var accountPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_remote)

        // TODO: 24.02.2019 add help icon for remote control
        accountPreference = requirePreference(PREF_REMOTE_CONTROL_ACCOUNT).apply {
            onPreferenceClickListener = OnPreferenceClickListener {
                authorizator.selectAccount()
                true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorizator = GoogleAuthorizationHelper(this, PREF_REMOTE_CONTROL_ACCOUNT, MAIL_GOOGLE_COM)
    }

    override fun onStart() {
        super.onStart()
        updateAccountPreferenceSummary()
        updatePreferences()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authorizator.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_REMOTE_CONTROL_ACCOUNT ->
                updateAccountPreferenceSummary()
            PREF_REMOTE_CONTROL_ENABLED -> {
                updatePreferences()
                enable(requireContext())
            }
        }
        super.onSharedPreferenceChanged(sharedPreferences, key)
    }

    private fun updateAccountPreferenceSummary() {
        val value = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT, null)
        if (isNullOrEmpty(value)) {
            updateSummary(accountPreference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else if (!authorizator.isAccountExists(value)) {
            updateSummary(accountPreference, value, SUMMARY_STYLE_UNDERWIVED)
        } else {
            updateSummary(accountPreference, value, SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun updatePreferences() {
        val enabled = settings.getBoolean(PREF_REMOTE_CONTROL_ENABLED, false)
        accountPreference.isEnabled = enabled
        requirePreference(PREF_REMOTE_CONTROL_NOTIFICATIONS).isEnabled = enabled
        requirePreference(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS).isEnabled = enabled
    }
}