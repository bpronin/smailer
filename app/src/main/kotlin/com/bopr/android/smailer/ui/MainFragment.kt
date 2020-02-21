package com.bopr.android.smailer.ui

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.SwitchPreference
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_HISTORY
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_RULES
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.ui.BatteryOptimizationHelper.requireIgnoreBatteryOptimization
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.getQuantityString
import com.bopr.android.smailer.util.isValidEmailAddressList
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

        requirePreference(PREF_RULES).setOnPreferenceClickListener {
            startActivity(Intent(context, RulesActivity::class.java))
            true
        }

        requirePreference(PREF_SENDER_ACCOUNT).setOnPreferenceClickListener {
            authorizator.selectAccount()
            true
        }

        requirePreference(PREF_RECIPIENTS_ADDRESS).setOnPreferenceClickListener {
            startActivity(Intent(context, RecipientsActivity::class.java))
            true
        }

        requirePreference(PREF_HISTORY).setOnPreferenceClickListener {
            startActivity(Intent(context, HistoryActivity::class.java))
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

        permissionsHelper.checkAll()
        requireIgnoreBatteryOptimization(requireContext())
    }

    override fun onDestroy() {
        database.close()
        unregisterDatabaseListener(requireContext(), databaseListener)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authorizator.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        updateAccountPreferenceView()
        updateRecipientsPreferenceView()
        updateDeviceNamePreferenceView()
        updateLocalePreferenceView()
        updateHistoryPreferenceView()
        updateShowNotificationPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            PREF_SENDER_ACCOUNT ->
                updateAccountPreferenceView()
            PREF_RECIPIENTS_ADDRESS ->
                updateRecipientsPreferenceView()
            PREF_EMAIL_LOCALE ->
                updateLocalePreferenceView()
            PREF_DEVICE_ALIAS ->
                updateDeviceNamePreferenceView()
            PREF_NOTIFY_SEND_SUCCESS ->
                updateShowNotificationPreferenceView()
        }
        super.onSharedPreferenceChanged(sharedPreferences, key)
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
            updateSummary(preference,
                    getQuantityString(R.plurals.new_history_items_count, count),
                    SUMMARY_STYLE_DEFAULT)
        } else {
            updateSummary(preference, null, SUMMARY_STYLE_DEFAULT)
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
        val preference = findPreference<EditTextPreference>(PREF_DEVICE_ALIAS)!!
        val value = settings.getString(preference.key)

        if (value.isNullOrEmpty()) {
            updateSummary(preference, deviceName(), SUMMARY_STYLE_DEFAULT)
        } else {
            updateSummary(preference, value, SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun updateShowNotificationPreferenceView() {
        val preference = findPreference<SwitchPreference>(PREF_NOTIFY_SEND_SUCCESS)!!
        preference.isChecked = settings.getBoolean(PREF_NOTIFY_SEND_SUCCESS, false)
    }
}