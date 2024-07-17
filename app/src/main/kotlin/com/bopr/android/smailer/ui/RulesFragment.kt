package com.bopr.android.smailer.ui

import android.content.BroadcastReceiver
import android.content.SharedPreferences
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.preference.MultiSelectListPreference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_BLACKLIST
import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_WHITELIST
import com.bopr.android.smailer.data.Database.Companion.TABLE_TEXT_BLACKLIST
import com.bopr.android.smailer.data.Database.Companion.TABLE_TEXT_WHITELIST
import com.bopr.android.smailer.data.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.data.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.util.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.SUMMARY_STYLE_DEFAULT
import com.bopr.android.smailer.util.updateSummary

/**
 * Rules settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RulesFragment : BasePreferenceFragment() {

    private lateinit var database: Database
    private lateinit var databaseListener: BroadcastReceiver

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_rules)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Database(requireContext())
        databaseListener = requireContext().registerDatabaseListener { tables ->
            if (tables.contains(TABLE_PHONE_BLACKLIST)) updatePhoneBlacklistPreferenceView()
            if (tables.contains(TABLE_PHONE_WHITELIST)) updatePhoneWhitelistPreferenceView()
            if (tables.contains(TABLE_TEXT_BLACKLIST)) updateTextBlacklistPreferenceView()
            if (tables.contains(TABLE_TEXT_WHITELIST)) updateTextWhitelistPreferenceView()
        }
    }

    override fun onDestroy() {
        requireContext().unregisterDatabaseListener(databaseListener)
        database.close()
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREF_EMAIL_TRIGGERS) {
            updateTriggersPreferenceView()
        }
    }

    private fun updatePhoneBlacklistPreferenceView() {
        val text = formatListSummary(
            database.phoneBlacklist,
            R.string.unacceptable_phone_numbers, R.string._none
        )

        requirePreference("phone_blacklist").updateSummary(text)
    }

    private fun updatePhoneWhitelistPreferenceView() {
        val text = formatListSummary(
            database.phoneWhitelist,
            R.string.acceptable_phone_numbers, R.string._any
        )

        requirePreference("phone_whitelist").updateSummary(text, SUMMARY_STYLE_DEFAULT)
    }

    private fun updateTextBlacklistPreferenceView() {
        val preference = requirePreference("text_blacklist")
        val text = formatListSummary(
            database.smsTextBlacklist,
            R.string.unacceptable_words, R.string._none
        )

        preference.updateSummary(text, SUMMARY_STYLE_DEFAULT)
    }

    private fun updateTextWhitelistPreferenceView() {
        val preference = requirePreference("text_whitelist")
        val formatListSummary = formatListSummary(
            database.smsTextWhitelist,
            R.string.acceptable_words, R.string._any
        )

        preference.updateSummary(formatListSummary, SUMMARY_STYLE_DEFAULT)
    }

    private fun updateTriggersPreferenceView() {
        requirePreferenceAs<MultiSelectListPreference>(PREF_EMAIL_TRIGGERS).apply {
            if (settings.getStringSet(PREF_EMAIL_TRIGGERS).isEmpty()) {
                updateSummary(R.string.no_triggers_specified, SUMMARY_STYLE_ACCENTED)
            } else {
                updateSummary(R.string.events_causing_sending_mail, SUMMARY_STYLE_DEFAULT)
            }
        }
    }

    private fun formatListSummary(
        list: Set<String>, @StringRes patternRes: Int,
        @StringRes emptyRes: Int
    ): String {
        return getString(patternRes, if (list.isEmpty()) getString(emptyRes) else list.size)
    }

}