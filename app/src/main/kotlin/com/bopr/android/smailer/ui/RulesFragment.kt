package com.bopr.android.smailer.ui

import android.content.BroadcastReceiver
import android.os.Bundle
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_TRIGGERS
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_BLACKLIST
import com.bopr.android.smailer.data.Database.Companion.TABLE_PHONE_WHITELIST
import com.bopr.android.smailer.data.Database.Companion.TABLE_TEXT_BLACKLIST
import com.bopr.android.smailer.data.Database.Companion.TABLE_TEXT_WHITELIST
import com.bopr.android.smailer.data.Database.Companion.registerDatabaseListener
import com.bopr.android.smailer.data.Database.Companion.unregisterDatabaseListener
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.updateSummary

/**
 * Rules settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RulesFragment : BasePreferenceFragment(R.xml.pref_rules) {

    private lateinit var database: Database
    private lateinit var databaseListener: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requirePreference(PREF_MAIL_TRIGGERS).setOnChangeListener {
            it.apply {
                if (settings.getStringSet(key).isEmpty()) {
                    updateSummary(R.string.no_triggers_specified, SUMMARY_STYLE_ACCENTED)
                } else {
                    updateSummary(R.string.email_processing_triggers)
                }
            }
        }

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

        updatePhoneBlacklistPreferenceView()
        updatePhoneWhitelistPreferenceView()
        updateTextBlacklistPreferenceView()
        updateTextWhitelistPreferenceView()
    }

    private fun updatePhoneBlacklistPreferenceView() {
        requirePreference(PREF_PHONE_BLACKLIST).updateSummary(
            formatListSummary(
                database.phoneBlacklist, R.string.unacceptable_phone_numbers, R.string._none
            )
        )
    }

    private fun updatePhoneWhitelistPreferenceView() {
        requirePreference(PREF_PHONE_WHITELIST).updateSummary(
            formatListSummary(
                database.phoneWhitelist, R.string.acceptable_phone_numbers, R.string._any
            )
        )
    }

    private fun updateTextBlacklistPreferenceView() {
        requirePreference(PREF_TEXT_BLACKLIST).updateSummary(
            formatListSummary(
                database.textBlacklist, R.string.unacceptable_words, R.string._none
            )
        )
    }

    private fun updateTextWhitelistPreferenceView() {
        requirePreference(PREF_TEXT_WHITELIST).updateSummary(
            formatListSummary(
                database.textWhitelist, R.string.acceptable_words, R.string._any
            )
        )
    }

    private fun formatListSummary(
        list: Set<String>, @StringRes patternRes: Int,
        @StringRes emptyRes: Int
    ): String {
        return getString(patternRes, if (list.isEmpty()) getString(emptyRes) else list.size)
    }

    companion object {

        private const val PREF_TEXT_WHITELIST = "text_whitelist"
        private const val PREF_TEXT_BLACKLIST = "text_blacklist"
        private const val PREF_PHONE_WHITELIST = "phone_whitelist"
        private const val PREF_PHONE_BLACKLIST = "phone_blacklist"
    }
}