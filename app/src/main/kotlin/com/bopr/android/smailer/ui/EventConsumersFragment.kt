package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.os.Bundle
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.external.Telegram
import com.bopr.android.smailer.external.TelegramException
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_BAD_RESPONSE
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_INVALID_TOKEN
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_NO_CHAT
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_NO_TOKEN
import com.bopr.android.smailer.external.TelegramException.Code.TELEGRAM_REQUEST_FAILED
import com.bopr.android.smailer.util.commaSplit
import com.bopr.android.smailer.util.showToast
import com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND

/**
 * Event consumers settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EventConsumersFragment : BasePreferenceFragment() {

    private lateinit var accountHelper: AccountHelper
    private lateinit var authorizationHelper: GoogleAuthorizationHelper
    //    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { _ ->
//        updateAccountPreferenceView()
//    }

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_event_consumers)

        requirePreference(PREF_SENDER_ACCOUNT).setOnPreferenceClickListener {
            authorizationHelper.startAccountPicker()
            true
        }

        /*     requirePreference(PREF_SENDER_ACCOUNT).setOnPreferenceClickListener {
                    authorizationHelper.startAccountSelectorActivity()
                    true
                }

                requirePreference("sent_test_email").setOnPreferenceClickListener {
                    sendTestMessage(it, GoogleMail(requireContext()))
                    true
                }
        */

//        requirePreference(PREF_TELEGRAM_BOT_TOKEN).setOnPreferenceClickListener {
//            todo: start token editor dialog
//            true
//        }

        requirePreference("sent_test_telegram_message").setOnPreferenceClickListener {
            sendTestTelegramMessage()
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountHelper = AccountHelper(requireContext())
        authorizationHelper = GoogleAuthorizationHelper(
            requireActivity(), PREF_SENDER_ACCOUNT, GMAIL_SEND, DRIVE_APPDATA
        )
    }

    override fun onStart() {
        super.onStart()

        updateAccountPreferenceView()
        updateRecipientsPreferenceView()
        updateTelegramBotTokenPreferenceView()
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        updateAccountPreferenceView()
//    }

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

    private fun sendTestTelegramMessage() {
        Telegram(requireContext()).sendMessage(
            "This is a test message",
            onSuccess = {
                /* if we live the page while processing then context becomes null. so "context?" */
                context?.showToast(R.string.test_message_sent)
            },
            onError = { error ->
                if (error is TelegramException) {
                    when (error.code) {
                        TELEGRAM_REQUEST_FAILED,
                        TELEGRAM_BAD_RESPONSE -> showInfoDialog(
                            R.string.test_message_failed,
                            R.string.error_sending_test_message
                        )

                        TELEGRAM_NO_TOKEN -> showInfoDialog(
                            R.string.test_message_failed,
                            R.string.no_telegram_bot_token
                        )

                        TELEGRAM_INVALID_TOKEN -> showInfoDialog(
                            R.string.test_message_failed,
                            R.string.bad_telegram_bot_token
                        )

                        TELEGRAM_NO_CHAT -> showInfoDialog(
                            R.string.test_message_failed,
                            R.string.require_start_chat
                        )
                    }
                } else throw error
            }
        )

//        preference.runLongTask(
//            onPerform = {
//                transport.sendMessages(
//                    EventMessage(
//                        subject = "${getString(R.string.app_name)} test message",
//                        text = "This is a test message"
//                    )
//                )
//            },
//            onComplete = { _, error ->
//                if (error != null) {
//                    val errorMessage = getString(R.string.error_sending_test_message)
//                    InfoDialog(message = errorMessage).show(requireActivity())
//                } else {
//                    /* if we live the page while processing then context becomes null. so "context?" */
//                    context?.showToast(R.string.operation_complete)
//                }
//            })
    }

    private fun updateAccountPreferenceView() {
        val preference = requirePreference(PREF_SENDER_ACCOUNT)
        val account = settings.getSenderAccountName()

        if (account.isNullOrEmpty()) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else if (!accountHelper.isGoogleAccountExists(account)) {
            updateSummary(preference, account, SUMMARY_STYLE_UNDERWIVED)
        } else {
            updateSummary(preference, account, SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun updateRecipientsPreferenceView() {
        val preference = requirePreference(PREF_RECIPIENTS_ADDRESS)
        val addresses = commaSplit(settings.getEmailRecipients())

        if (addresses.isEmpty()) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else if (addresses.size == 1) {
            updateSummary(preference, addresses.first(), SUMMARY_STYLE_DEFAULT)
        } else {
            updateSummary(
                preference,
                getString(R.string.addresses, addresses.size),
                SUMMARY_STYLE_DEFAULT
            )
        }
    }


    private fun updateTelegramBotTokenPreferenceView() {
        val preference = requirePreference(PREF_TELEGRAM_BOT_TOKEN)
        val token = settings.getTelegramBotToken()

        if (token.isNullOrEmpty()) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else {
            updateSummary(preference, getString(R.string.specified), SUMMARY_STYLE_DEFAULT)
        }
    }
}