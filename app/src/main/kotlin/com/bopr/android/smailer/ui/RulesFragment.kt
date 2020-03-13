package com.bopr.android.smailer.ui

import android.content.BroadcastReceiver
import android.content.SharedPreferences
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.preference.MultiSelectListPreference
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.TABLE_PHONE_BLACKLIST
import com.bopr.android.smailer.Database.Companion.TABLE_PHONE_WHITELIST
import com.bopr.android.smailer.Database.Companion.TABLE_TEXT_BLACKLIST
import com.bopr.android.smailer.Database.Companion.TABLE_TEXT_WHITELIST
import com.bopr.android.smailer.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_PHONE_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_TEXT_WHITELIST

/**
 * Rules settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RulesFragment : BasePreferenceFragment() {

    private lateinit var databaseListener: BroadcastReceiver
    private lateinit var database: Database

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_rules)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Database(requireContext())
        databaseListener = requireContext().registerDatabaseListener {
            if (it.contains(TABLE_PHONE_BLACKLIST)) updatePhoneBlacklistPreferenceView()
            if (it.contains(TABLE_PHONE_WHITELIST)) updatePhoneWhitelistPreferenceView()
            if (it.contains(TABLE_TEXT_BLACKLIST)) updateTextBlacklistPreferenceView()
            if (it.contains(TABLE_TEXT_WHITELIST)) updateTextWhitelistPreferenceView()
        }
    }

    override fun onDestroy() {
        database.close()
        requireContext().unregisterDatabaseListener(databaseListener)
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()

        updateTriggersPreferenceView()
        updatePhoneBlacklistPreferenceView()
        updatePhoneWhitelistPreferenceView()
        updateTextBlacklistPreferenceView()
        updateTextWhitelistPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        if (key == PREF_EMAIL_TRIGGERS) {
            updateTriggersPreferenceView()
        }
    }

    private fun updateTextWhitelistPreferenceView() {
        val preference = requirePreference(PREF_TEXT_WHITELIST)
        val value = settings.getStringList(preference.key)
        val formatListSummary = formatListSummary(value, R.string.acceptable_words, R.string._any)

        updateSummary(preference, formatListSummary, SUMMARY_STYLE_DEFAULT)
    }

    private fun updateTextBlacklistPreferenceView() {
        val preference = requirePreference(PREF_TEXT_BLACKLIST)
        val value = settings.getStringList(preference.key)
        val text = formatListSummary(value, R.string.unacceptable_words, R.string._none)

        updateSummary(preference, text, SUMMARY_STYLE_DEFAULT)
    }

    private fun updatePhoneWhitelistPreferenceView() {
        val preference = requirePreference(PREF_PHONE_WHITELIST)
        val value = settings.getStringList(preference.key)
        val text = formatListSummary(value, R.string.acceptable_phone_numbers, R.string._any)

        updateSummary(preference, text, SUMMARY_STYLE_DEFAULT)
    }

    private fun updatePhoneBlacklistPreferenceView() {
        val preference = requirePreference(PREF_PHONE_BLACKLIST)
        val value = settings.getStringList(preference.key)
        val text = formatListSummary(value, R.string.unacceptable_phone_numbers, R.string._none)

        updateSummary(preference, text, SUMMARY_STYLE_DEFAULT)
    }

    private fun updateTriggersPreferenceView() {
        val preference: MultiSelectListPreference = findPreference(PREF_EMAIL_TRIGGERS)!!
        val value = settings.getStringSet(preference.key)

        if (value.isEmpty()) {
            updateSummary(preference, getString(R.string.no_triggers_specified), SUMMARY_STYLE_ACCENTED)
        } else {
            updateSummary(preference, getString(R.string.events_causing_sending_mail), SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun formatListSummary(list: List<String>, @StringRes patternRes: Int,
                                  @StringRes emptyRes: Int): String {
        return getString(patternRes, if (list.isEmpty()) getString(emptyRes) else list.size)
    }

}