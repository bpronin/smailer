package com.bopr.android.smailer.ui

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_HISTORY
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.ui.BatteryOptimizationHelper.requireIgnoreBatteryOptimization
import com.bopr.android.smailer.util.AndroidUtil.deviceName
import com.bopr.android.smailer.util.TextUtil.isValidEmailAddressList
import com.bopr.android.smailer.util.UiUtil.getQuantityString
import com.google.api.services.drive.DriveScopes
import com.google.api.services.gmail.GmailScopes

/**
 * Main settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainFragment : BasePreferenceFragment() {

    private lateinit var database: Database
    private lateinit var databaseListener: BroadcastReceiver
    private lateinit var authorizator: GoogleAuthorizationHelper

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)

        requirePreference(PREF_SENDER_ACCOUNT).setOnPreferenceClickListener {
            authorizator.startAccountSelectorActivity()
            true
        }

        findPreference<EditTextPreference>(PREF_DEVICE_ALIAS)!!.setOnBindEditTextListener { editText ->
            editText.hint = deviceName()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authorizator = GoogleAuthorizationHelper(this, PREF_SENDER_ACCOUNT, GmailScopes.GMAIL_SEND,
                DriveScopes.DRIVE_APPDATA)

        database = Database(requireContext())
        databaseListener = registerDatabaseListener(requireContext()) {
            updateHistoryPreferenceView()
        }

        permissionsHelper.checkAll {
            requireIgnoreBatteryOptimization(requireContext())
        }
    }

    override fun onDestroy() {
        database.close()
        unregisterDatabaseListener(requireContext(), databaseListener)
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()

        updateAccountPreferenceView()
        updateRecipientsPreferenceView()
        updateDeviceNamePreferenceView()
        updateLocalePreferenceView()
        updateHistoryPreferenceView()
        updateRemoteControlPreferenceView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authorizator.onAccountSelectorActivityResult(requestCode, resultCode, data)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            PREF_SENDER_ACCOUNT ->
                updateAccountPreferenceView()
            PREF_RECIPIENTS_ADDRESS ->
                updateRecipientsPreferenceView()
            PREF_DEVICE_ALIAS ->
                updateDeviceNamePreferenceView()
            PREF_EMAIL_LOCALE ->
                updateLocalePreferenceView()
            PREF_REMOTE_CONTROL_ENABLED ->
                updateRemoteControlPreferenceView()
        }
    }

    private fun updateAccountPreferenceView() {
        val preference = requirePreference(PREF_SENDER_ACCOUNT)
        val value = settings.getString(preference.key)

        if (value.isNullOrEmpty()) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else if (!authorizator.isAccountExists(value)) {
            updateSummary(preference, value, SUMMARY_STYLE_UNDERWIVED)
        } else {
            updateSummary(preference, value, SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun updateRecipientsPreferenceView() {
        val preference = requirePreference(PREF_RECIPIENTS_ADDRESS)
        val value = settings.getString(preference.key)

        if (value.isNullOrEmpty()) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else {
            val style = if (isValidEmailAddressList(value)) SUMMARY_STYLE_DEFAULT else SUMMARY_STYLE_UNDERWIVED
            updateSummary(preference, value.replace(",", ", "), style)
        }
    }

    private fun updateHistoryPreferenceView() {
        val preference = requirePreference(PREF_HISTORY)
        val count = database.unreadEventsCount
        if (count > 0) {
            updateSummary(preference, getQuantityString(R.plurals.new_history_items_count, count), SUMMARY_STYLE_DEFAULT)
        } else {
            updateSummary(preference, getString(R.string.no_new_history_items), SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun updateLocalePreferenceView() {
        val preference = findPreference<ListPreference>(PREF_EMAIL_LOCALE)!!
        val value = settings.getString(preference.key)

        val index = preference.findIndexOfValue(value)
        if (index < 0) {
            updateSummary(preference, getString(R.string.not_specified), SUMMARY_STYLE_ACCENTED)
        } else {
            updateSummary(preference, preference.entries[index], SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun updateDeviceNamePreferenceView() {
        val preference = requirePreference(PREF_DEVICE_ALIAS)
        val alias = settings.getString(preference.key)

        if (alias == null) {
            updateSummary(preference, deviceName(), SUMMARY_STYLE_DEFAULT)
        } else {
            updateSummary(preference, alias, SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun updateRemoteControlPreferenceView() {
        val preference = requirePreference(PREF_REMOTE_CONTROL_ENABLED)
        if (settings.getBoolean(preference.key, false)) {
            updateSummary(preference, getString(R.string.enabled), SUMMARY_STYLE_DEFAULT)
        } else {
            updateSummary(preference, getString(R.string.disavbled), SUMMARY_STYLE_DEFAULT)
        }
    }
}
