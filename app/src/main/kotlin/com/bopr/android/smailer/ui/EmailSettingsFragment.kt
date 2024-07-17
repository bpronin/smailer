package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_MESSAGE_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.processor.mail.MailMessage
import com.bopr.android.smailer.external.GoogleMail
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.SUMMARY_STYLE_DEFAULT
import com.bopr.android.smailer.util.SUMMARY_STYLE_UNDERWIVED
import com.bopr.android.smailer.util.commaSplit
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.runLongTask
import com.bopr.android.smailer.util.showToast
import com.bopr.android.smailer.util.updateSummary
import com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND

/**
 * Email settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EmailSettingsFragment : BasePreferenceFragment() {

    private lateinit var accountHelper: AccountHelper
    private lateinit var authorizationHelper: GoogleAuthorizationHelper
    //    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { _ ->
//        updateAccountPreferenceView()
//    }

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_email_settings)

        requirePreference(PREF_EMAIL_SENDER_ACCOUNT).setOnPreferenceClickListener {
            authorizationHelper.startAccountPicker()
            true
        }

        requirePreference("sent_test_email").setOnPreferenceClickListener {
            onSendTestMessage(it)
            true
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountHelper = AccountHelper(requireContext())
        authorizationHelper = GoogleAuthorizationHelper(
            requireActivity(), PREF_EMAIL_SENDER_ACCOUNT, GMAIL_SEND, DRIVE_APPDATA
        )
    }

    override fun onStart() {
        super.onStart()

        updateEmailPreferenceView()
        updateAccountPreferenceView()
        updateRecipientsPreferenceView()
        updateLocalePreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_MESSAGE_LOCALE ->
                updateLocalePreferenceView()

            PREF_EMAIL_SENDER_ACCOUNT ->
                updateAccountPreferenceView()

            PREF_RECIPIENTS_ADDRESS ->
                updateRecipientsPreferenceView()

            PREF_EMAIL_MESSENGER_ENABLED ->
                updateEmailPreferenceView()

        }
    }

    private fun onSendTestMessage(preference: Preference) {
        preference.runLongTask(
            onPerform = {
                val account = accountHelper.requirePrimaryGoogleAccount()

                val message = MailMessage(
                    from = account.name,
                    subject = "Test",
                    body = "This is test message from $DEVICE_NAME",
                    recipients = settings.getEmailRecipients()
                )

                GoogleMail(requireContext(), account, GMAIL_SEND).send(message)
            },
            onSuccess = {
                showToast(R.string.test_message_sent)
            },
            onError = { _ ->
                showInfoDialog(R.string.test_message_failed)
            }
        )
    }


    //    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        updateAccountPreferenceView()
//    }

    private fun updateEmailPreferenceView() {
        requirePreference(PREF_EMAIL_MESSENGER_ENABLED).apply {
            setTitle(onOffText(settings.getBoolean(PREF_EMAIL_MESSENGER_ENABLED)))
        }
    }

    private fun updateLocalePreferenceView() {
        requirePreferenceAs<ListPreference>(PREF_MESSAGE_LOCALE).apply {
            val index = findIndexOfValue(settings.getMessageLocale())
            if (index < 0) {
                updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
            } else {
                updateSummary(entries[index], SUMMARY_STYLE_DEFAULT)
            }
        }
    }

    private fun updateAccountPreferenceView() {
        requirePreference(PREF_EMAIL_SENDER_ACCOUNT).apply {
            val account = settings.getString(PREF_EMAIL_SENDER_ACCOUNT)
            if (account.isNullOrEmpty()) {
                updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
            } else if (!accountHelper.isGoogleAccountExists(account)) {
                updateSummary(account, SUMMARY_STYLE_UNDERWIVED)
            } else {
                updateSummary(account, SUMMARY_STYLE_DEFAULT)
            }
        }
    }

    private fun updateRecipientsPreferenceView() {
        requirePreference(PREF_RECIPIENTS_ADDRESS).apply {
            val addresses = commaSplit(settings.getEmailRecipients())
            if (addresses.isEmpty()) {
                updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
            } else if (addresses.size == 1) {
                updateSummary(addresses.first(), SUMMARY_STYLE_DEFAULT)
            } else {
                updateSummary(getString(R.string.addresses, addresses.size), SUMMARY_STYLE_DEFAULT)
            }
        }
    }
}
