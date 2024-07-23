package com.bopr.android.smailer.ui

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
import com.bopr.android.smailer.processor.mail.GoogleMail
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.SUMMARY_STYLE_DEFAULT
import com.bopr.android.smailer.util.SUMMARY_STYLE_UNDERWIVED
import com.bopr.android.smailer.util.commaSplit
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.requirePreferenceAs
import com.bopr.android.smailer.util.runLongTask
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.showToast
import com.bopr.android.smailer.util.updateSummary
import com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND

/**
 * Email settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EmailSettingsFragment : BasePreferenceFragment(R.xml.pref_email_settings) {

    private lateinit var accountHelper: AccountHelper
    private lateinit var authorizationHelper: GoogleAuthorizationHelper
    //    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { _ ->
//        updateAccountPreferenceView()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requirePreferenceAs<ListPreference>(PREF_MESSAGE_LOCALE).setOnChangeListener {
            it.apply {
                val index = findIndexOfValue(settings.getMessageLocale())
                if (index < 0) {
                    updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
                } else {
                    updateSummary(entries[index], SUMMARY_STYLE_DEFAULT)
                }
            }
        }

        requirePreference(PREF_EMAIL_SENDER_ACCOUNT).apply {
            setOnChangeListener {
                updateAccountPreferenceView()
            }
            setOnClickListener {
                authorizationHelper.startAccountPicker()
            }
        }

        requirePreference(PREF_RECIPIENTS_ADDRESS).setOnChangeListener {
            it.apply {
                val addresses = commaSplit(settings.getEmailRecipients())
                if (addresses.isEmpty()) {
                    updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
                } else if (addresses.size == 1) {
                    updateSummary(addresses.first(), SUMMARY_STYLE_DEFAULT)
                } else {
                    updateSummary(
                        getString(R.string.addresses, addresses.size),
                        SUMMARY_STYLE_DEFAULT
                    )
                }
            }
        }

        requirePreference(PREF_EMAIL_MESSENGER_ENABLED).setOnChangeListener {
            it.apply {
                setTitle(onOffText(settings.getBoolean(PREF_EMAIL_MESSENGER_ENABLED)))
            }
        }

        requirePreference(PREF_SENT_TEST_EMAIL).setOnClickListener {
            onSendTestMessage(it)
        }

        accountHelper = AccountHelper(requireContext())
        authorizationHelper = GoogleAuthorizationHelper(
            requireActivity(), PREF_EMAIL_SENDER_ACCOUNT, GMAIL_SEND, DRIVE_APPDATA
        )
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

     companion object{

         private const val PREF_SENT_TEST_EMAIL = "sent_test_email"
     }
}
