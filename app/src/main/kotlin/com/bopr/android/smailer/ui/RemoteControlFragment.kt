package com.bopr.android.smailer.ui

import android.os.Bundle
import androidx.preference.Preference
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_NOTIFICATIONS
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.control.mail.MailControlProcessor
import com.bopr.android.smailer.ui.InfoDialog.Companion.showInfoDialog
import com.bopr.android.smailer.util.PreferenceProgress
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_UNDERWIVED
import com.bopr.android.smailer.util.getQuantityString
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.showToast
import com.bopr.android.smailer.util.updateSummary
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM

// TODO: 24.02.2019 add help icon for remote control
/**
 * Remote control settings fragment.
 * 
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class RemoteControlFragment : BasePreferenceFragment(R.xml.pref_remote) {

    private lateinit var authorizationHelper: GoogleAuthorizationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authorizationHelper = GoogleAuthorizationHelper(
            requireActivity(), PREF_REMOTE_CONTROL_ACCOUNT, MAIL_GOOGLE_COM
        )

        requirePreference(PREF_REMOTE_CONTROL_ACCOUNT).setOnClickListener {
            authorizationHelper.startAccountPicker()
        }

        requirePreference(PREF_PROCESS_SERVICE_MAIL).setOnClickListener {
            onProcessServiceMail(it)
        }

        requirePreference(PREF_REMOTE_CONTROL_ENABLED).setOnChangeListener {
            val value = settings.getBoolean(it.key)
            requirePreference(PREF_REMOTE_CONTROL_ACCOUNT).isEnabled = value
            requirePreference(PREF_REMOTE_CONTROL_NOTIFICATIONS).isEnabled = value
            requirePreference(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS).isEnabled = value
            requirePreference(PREF_PROCESS_SERVICE_MAIL).isEnabled = value
        }

        requirePreference(PREF_REMOTE_CONTROL_ACCOUNT).setOnChangeListener {
            val account = settings.getString(it.key)
            it.apply {
                if (account.isNullOrEmpty()) {
                    updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
                } else if (!requireContext().accounts.isGoogleAccountExists(account)) {
                    updateSummary(account, SUMMARY_STYLE_UNDERWIVED)
                } else {
                    updateSummary(account)
                }
            }
        }
    }

    private fun onProcessServiceMail(preference: Preference) {
        val progress = PreferenceProgress(preference).apply { start() }
        MailControlProcessor(requireContext()).checkMailbox(
            onSuccess = { result ->
                progress.stop()
                showToast(
                    getQuantityString(R.plurals.mail_items, R.string.mail_items_zero, result)
                )
            },
            onError = { error ->
                progress.stop()
                showInfoDialog(
                    getString(R.string.remote_control),
                    error.message ?: error.toString()
                )
            }
        )
    }

    companion object {

        const val PREF_PROCESS_SERVICE_MAIL = "remote_control_process_service_mail"
    }
}