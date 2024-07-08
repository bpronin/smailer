package com.bopr.android.smailer.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.sender.EventMessage
import com.bopr.android.smailer.sender.GoogleMail
import com.bopr.android.smailer.sender.MessengerTransport
import com.bopr.android.smailer.sender.TelegramBot
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

//        requirePreference(PREF_TELEGRAM_BOT_TOKEN).setOnPreferenceClickListener {
//            todo: start token editor dialog
//            true
//        }

        requirePreference("sent_test_email").onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                sendTestMessage(it, GoogleMail(requireContext()))
                true
            }

        requirePreference("sent_test_telegram_message").onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                sendTestMessage(it, TelegramBot(requireContext()))
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
        updateTelegramBotTokenPreferenceView()
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

            PREF_TELEGRAM_BOT_TOKEN ->
                updateTelegramBotTokenPreferenceView()
        }
    }

    private fun sendTestMessage(preference: Preference, transport: MessengerTransport) {
        preference.runLongTask(
            onPerform = {
                transport.sendMessages(
                    EventMessage(
                        subject = "${getString(R.string.app_name)} test message",
                        text = "This is a test message"
                    )
                )
            },
            onComplete = { _, error ->
                if (error != null) {
                    val errorMessage = getString(R.string.error_sending_test_message)
                    InfoDialog(message = errorMessage).show(requireActivity())
                } else {
                    /* if we live the page while processing then context becomes null. so "context?" */
                    context?.showToast(R.string.operation_complete)
                }
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

    private fun updateTelegramBotTokenPreferenceView() {
        val preference = requirePreference(PREF_TELEGRAM_BOT_TOKEN)
        val token = settings.telegramBotToken

        if (token.isNullOrEmpty()) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else {
            updateSummary(preference, token, SUMMARY_STYLE_DEFAULT)
        }
    }
}