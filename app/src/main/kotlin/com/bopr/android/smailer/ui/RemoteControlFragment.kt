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
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.util.getQuantityString
import com.bopr.android.smailer.util.runLongTask
import com.bopr.android.smailer.util.showToast
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
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            PREF_REMOTE_CONTROL_ENABLED -> updateEnabledPreferenceView()

            PREF_REMOTE_CONTROL_ACCOUNT -> updateAccountPreferenceView()
        }
    }

    private fun onProcessServiceMail(preference: Preference) {
        preference.runLongTask(
            onPerform = {
                requireContext().run {
                    Database(this).use {
                        MailControlProcessor(this).checkMailbox()
                    }
                }
            },
            onComplete = { result, error ->
                /* if we live the page while processing then context becomes null. so "context?" */
                if (error != null) {
                    val errorMessage = error.message ?: error.toString()
                    requireActivity().showInfoDialog(message = errorMessage)
                } else
                    context?.showToast(
                        getQuantityString(
                            R.plurals.mail_items,
                            R.string.mail_items_zero,
                            result!!
                        )
                    )
            }
        )
    }

    private fun updateAccountPreferenceView() {
        val preference = requirePreference(PREF_REMOTE_CONTROL_ACCOUNT)
        val account = settings.getRemoteControlAccountName()

        if (account.isNullOrEmpty()) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else if (!accountHelper.isGoogleAccountExists(account)) {
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