package com.bopr.android.smailer.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.sender.EventMessage
import com.bopr.android.smailer.sender.GoogleMail
import com.bopr.android.smailer.util.isAccountExists
import com.bopr.android.smailer.util.isValidEmailAddressList
import com.bopr.android.smailer.util.runLongTask
import com.bopr.android.smailer.util.showToast
import com.google.api.services.drive.DriveScopes
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND

/**
 * Messengers settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MessengersFragment : BasePreferenceFragment() {

    private lateinit var authorizationHelper: GoogleAuthorizationHelper

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_messengers)

        requirePreference(PREF_SENDER_ACCOUNT).setOnPreferenceClickListener {
            authorizationHelper.startAccountSelectorActivity()
            true
        }

        requirePreference("sent_test_email").onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                onSendTestEmail(it)
                true
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorizationHelper = GoogleAuthorizationHelper(
            this, PREF_SENDER_ACCOUNT, GMAIL_SEND,
            DriveScopes.DRIVE_APPDATA
        )
    }

    override fun onStart() {
        super.onStart()

        updateAccountPreferenceView()
        updateRecipientsPreferenceView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authorizationHelper.onAccountSelectorActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        updateAccountPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            PREF_SENDER_ACCOUNT ->
                updateAccountPreferenceView()

            PREF_RECIPIENTS_ADDRESS ->
                updateRecipientsPreferenceView()
        }
    }

    private fun onSendTestEmail(preference: Preference) {
        preference.runLongTask(
            onPerform = {
                GoogleMail(requireContext()).sendMessages(
                    EventMessage(
                        subject = "${getString(R.string.app_name)} sample message",
                        text = "This is a sample message"
                    )
                )
            },
            onComplete = { _, error ->
                /* if we live the page while processing then context becomes null. so "context?" */
                if (error != null)
                    InfoDialog(error.message).show(requireActivity())
                else
                    context?.showToast(R.string.operation_complete)
            })
    }

    private fun updateAccountPreferenceView() {
        val preference = requirePreference(PREF_SENDER_ACCOUNT)
        val account = settings.senderAccount

        if (account.isNullOrEmpty()) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else if (!requireContext().isAccountExists(account)) {
            updateSummary(preference, account, SUMMARY_STYLE_UNDERWIVED)
        } else {
            updateSummary(preference, account, SUMMARY_STYLE_DEFAULT)
        }
    }


    private fun updateRecipientsPreferenceView() {
        val preference = requirePreference(PREF_RECIPIENTS_ADDRESS)
        val addresses = settings.emailRecipients

        if (addresses.isEmpty()) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else {
            val style =
                if (isValidEmailAddressList(addresses)) SUMMARY_STYLE_DEFAULT else SUMMARY_STYLE_UNDERWIVED
            updateSummary(preference, addresses.joinToString(), style)
        }
    }
}