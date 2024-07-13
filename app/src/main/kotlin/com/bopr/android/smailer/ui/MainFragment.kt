package com.bopr.android.smailer.ui

import android.content.BroadcastReceiver
import android.content.SharedPreferences
import android.os.Bundle
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.TABLE_EVENTS
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.util.getQuantityString
import com.bopr.android.smailer.util.isAccountExists
import com.bopr.android.smailer.util.isValidEmailAddressList
import com.google.api.services.drive.DriveScopes
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND

/**
 * Main settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainFragment : BasePreferenceFragment() {

    private lateinit var database: Database
    private lateinit var databaseListener: BroadcastReceiver
    private lateinit var authorizationHelper: GoogleAuthorizationHelper

//    private val requestPermissionLauncher = registerForActivityResult(RequestPermission()) { _ ->
//        updateAccountPreferenceView()
//    }

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)

        requirePreference(PREF_SENDER_ACCOUNT).setOnPreferenceClickListener {
            authorizationHelper.startAccountPicker()
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorizationHelper = GoogleAuthorizationHelper(
            this, PREF_SENDER_ACCOUNT, GMAIL_SEND,
            DriveScopes.DRIVE_APPDATA
        )

        val context = requireContext()

        database = Database(context)
        databaseListener = context.registerDatabaseListener { tables ->
            if (tables.contains(TABLE_EVENTS)) updateHistoryPreferenceView()
        }
    }

    override fun onDestroy() {
        requireContext().unregisterDatabaseListener(databaseListener)
        database.close()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()

        updateAccountPreferenceView()
        updateRecipientsPreferenceView()
        updateHistoryPreferenceView()
        updateRemoteControlPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            PREF_SENDER_ACCOUNT -> updateAccountPreferenceView()

            PREF_RECIPIENTS_ADDRESS -> updateRecipientsPreferenceView()

            PREF_REMOTE_CONTROL_ENABLED -> updateRemoteControlPreferenceView()
        }
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

    private fun updateHistoryPreferenceView() {
        val preference = requirePreference("history")
        updateSummary(
            preference,
            getQuantityString(
                R.plurals.new_history_items, R.string.new_history_items_zero,
                database.events.unreadCount
            ), SUMMARY_STYLE_DEFAULT
        )
    }

    private fun updateRemoteControlPreferenceView() {
        val preference = requirePreference(PREF_REMOTE_CONTROL_ENABLED)
        val enabled = settings.getBoolean(preference.key)
        updateSummary(
            preference,
            getString(if (enabled) R.string.enabled else R.string.disabled),
            SUMMARY_STYLE_DEFAULT
        )
    }
}
