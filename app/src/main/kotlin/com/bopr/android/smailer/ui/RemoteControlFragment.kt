package com.bopr.android.smailer.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_NOTIFICATIONS
import com.bopr.android.smailer.remote.RemoteControlProcessor
import com.bopr.android.smailer.util.getQuantityString
import com.bopr.android.smailer.util.isAccountExists
import com.bopr.android.smailer.util.runBackgroundTask
import com.bopr.android.smailer.util.showToast
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM

// TODO: 24.02.2019 add help icon for remote control
class RemoteControlFragment : BasePreferenceFragment() {

    private val processMailAction = "remote_control_process_service_mail"
    private lateinit var authorizator: GoogleAuthorizationHelper

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_remote)

        requirePreference(PREF_REMOTE_CONTROL_ACCOUNT).setOnPreferenceClickListener {
            authorizator.startAccountSelectorActivity()
            true
        }

        requirePreference("remote_control_process_service_mail").setOnPreferenceClickListener {
            onProcessServiceMail(it)
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorizator = GoogleAuthorizationHelper(this, PREF_REMOTE_CONTROL_ACCOUNT, MAIL_GOOGLE_COM)
    }

    override fun onStart() {
        super.onStart()

        updateAccountPreferenceView()
        updateEnabledPreferenceView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authorizator.onAccountSelectorActivityResult(requestCode, resultCode, data)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            PREF_REMOTE_CONTROL_ENABLED ->
                updateEnabledPreferenceView()
            PREF_REMOTE_CONTROL_ACCOUNT ->
                updateAccountPreferenceView()
        }
    }

    private fun onProcessServiceMail(preference: Preference) {
        preference.runBackgroundTask(
                onPerform = {
                    requireContext().run {
                        Database(this).use {
                            RemoteControlProcessor(this, it).checkMailbox()
                        }
                    }
                },
                onComplete = {
                    /* NOTE: if we live the page while processing context becomes null */
                    context?.showToast(getQuantityString(R.plurals.mail_items, R.string.mail_items_zero, it!!))
                }
        )
    }

    private fun updateAccountPreferenceView() {
        val preference = requirePreference(PREF_REMOTE_CONTROL_ACCOUNT)
        val account = settings.remoteControlAccount

        if (account.isNullOrEmpty()) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else if (!requireContext().isAccountExists(account)) {
            updateSummary(preference, account, SUMMARY_STYLE_UNDERWIVED)
        } else {
            updateSummary(preference, account, SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun updateEnabledPreferenceView() {
        val preference: SwitchPreference = findPreference(PREF_REMOTE_CONTROL_ENABLED)!!
        val value = settings.getBoolean(preference.key)

        requirePreference(PREF_REMOTE_CONTROL_ACCOUNT).isEnabled = value
        requirePreference(PREF_REMOTE_CONTROL_NOTIFICATIONS).isEnabled = value
        requirePreference(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS).isEnabled = value
        requirePreference(processMailAction).isEnabled = value
    }
}