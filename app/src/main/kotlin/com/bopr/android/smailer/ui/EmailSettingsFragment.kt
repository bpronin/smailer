package com.bopr.android.smailer.ui

import android.os.Bundle
import android.text.TextUtils
import androidx.preference.ExtMultiSelectListPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSAGE_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_MESSAGE_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.processor.mail.BaseMailFormatter
import com.bopr.android.smailer.processor.mail.GoogleMailSession
import com.bopr.android.smailer.processor.mail.MailMessage
import com.bopr.android.smailer.ui.InfoDialog.Companion.showInfoDialog
import com.bopr.android.smailer.util.GeoLocation
import com.bopr.android.smailer.util.GeoLocation.Companion.requestGeoLocation
import com.bopr.android.smailer.util.PreferenceProgress
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_DEFAULT
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_UNDERWIVED
import com.bopr.android.smailer.util.commaSplit
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.requirePreferenceAs
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.titles
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requirePreferenceAs<ExtMultiSelectListPreference>(PREF_EMAIL_MESSAGE_CONTENT).apply {
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            setOnChangeListener {
                it.apply {
                    updateSummary(titles().joinToString(", "))
                }
            }
        }

        requirePreferenceAs<ListPreference>(PREF_MESSAGE_LOCALE).setOnChangeListener {
            it.apply {
                val index = findIndexOfValue(settings.getMessageLocale())
                if (index < 0) {
                    updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
                } else {
                    updateSummary(entries[index])
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

        requirePreference(PREF_EMAIL_MESSENGER_RECIPIENTS).setOnChangeListener {
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
        val account = accountHelper.getPrimaryGoogleAccount() ?: run {
            showInfoDialog(R.string.sender_account_not_found)
            return
        }

        val progress = PreferenceProgress(preference).apply { start() }
        val time = System.currentTimeMillis()

        requireContext().requestGeoLocation { location ->
            val formatter = TestMailFormatter(time, location)
            GoogleMailSession(requireContext(), account, GMAIL_SEND).send(
                MailMessage(
                    from = account.name,
                    subject = formatter.formatSubject(),
                    body = formatter.formatBody(),
                    recipients = settings.getEmailRecipients()
                ),
                onSuccess = {
                    progress.stop()
                    showInfoDialog(R.string.test_message_sent)
                },
                onError = {
                    progress.stop()
                    showInfoDialog(R.string.test_message_failed)
                }
            )
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
                updateSummary(account)
            }
        }
    }

    private inner class TestMailFormatter(time: Long, location: GeoLocation?) :
        BaseMailFormatter(requireContext(), time, time, location) {

        override fun getSubject(): String {
            return getString(R.string.test_message)
        }

        override fun getTitle(): String {
            return getString(R.string.test_message)
        }

        override fun getMessage(): String {
            return getString(R.string.this_is_test_message)
        }

        override fun getSenderName(): String {
            return getString(R.string.sender_of, getString(R.string.app_name))
        }

        override fun getReplyLinks(): List<String>? {
            return null
        }

    }

    companion object {

        private const val PREF_SENT_TEST_EMAIL = "send_test_email"
    }
}
