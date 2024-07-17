package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_NOTIFICATIONS
import com.bopr.android.smailer.control.MailControlProcessor
import com.bopr.android.smailer.util.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.SUMMARY_STYLE_DEFAULT
import com.bopr.android.smailer.util.SUMMARY_STYLE_UNDERWIVED
import com.bopr.android.smailer.util.getQuantityString
import com.bopr.android.smailer.util.runLongTask
import com.bopr.android.smailer.util.showToast
import com.bopr.android.smailer.util.updateSummary
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM

// TODO: 24.02.2019 add help icon for remote control
class RemoteControlFragment : BasePreferenceFragment() {

    private val processMailAction = "remote_control_process_service_mail"
    private lateinit var authorizationHelper: GoogleAuthorizationHelper
    private lateinit var accountHelper: AccountHelper

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_remote)

        requirePreference(PREF_REMOTE_CONTROL_ACCOUNT).setOnPreferenceClickListener {
            authorizationHelper.startAccountPicker()
            true
        }

        requirePreference("remote_control_process_service_mail").setOnPreferenceClickListener {
            onProcessServiceMail(it)
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountHelper = AccountHelper(requireContext())
        authorizationHelper = GoogleAuthorizationHelper(
            requireActivity(), PREF_REMOTE_CONTROL_ACCOUNT, MAIL_GOOGLE_COM
        )
    }

    override fun onStart() {
        super.onStart()

        updateAccountPreferenceView()
        updateEnabledPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_REMOTE_CONTROL_ENABLED -> updateEnabledPreferenceView()

            PREF_REMOTE_CONTROL_ACCOUNT -> updateAccountPreferenceView()
        }
    }

    private fun onProcessServiceMail(preference: Preference) {
        preference.runLongTask(
            onPerform = {
                MailControlProcessor(requireContext()).checkMailbox()
            },
            onSuccess = { result ->
                showToast(
                    getQuantityString(R.plurals.mail_items, R.string.mail_items_zero, result)
                )
            },
            onError = { error ->
                showInfoDialog(
                    getString(R.string.remote_control),
                    error.message ?: error.toString()
                )
            }
        )
    }

    private fun updateAccountPreferenceView() {
        requirePreference(PREF_REMOTE_CONTROL_ACCOUNT).apply {
            val account = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT)
            if (account.isNullOrEmpty()) {
                updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
            } else if (!accountHelper.isGoogleAccountExists(account)) {
                updateSummary(account, SUMMARY_STYLE_UNDERWIVED)
            } else {
                updateSummary(account, SUMMARY_STYLE_DEFAULT)
            }
        }
    }

    private fun updateEnabledPreferenceView() {
        val preference = requirePreferenceAs<SwitchPreference>(PREF_REMOTE_CONTROL_ENABLED)
        val value = settings.getBoolean(preference.key)

        requirePreference(PREF_REMOTE_CONTROL_ACCOUNT).isEnabled = value
        requirePreference(PREF_REMOTE_CONTROL_NOTIFICATIONS).isEnabled = value
        requirePreference(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS).isEnabled = value
        requirePreference(processMailAction).isEnabled = value
    }
}