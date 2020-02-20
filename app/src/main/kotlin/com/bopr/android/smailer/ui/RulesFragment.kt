package com.bopr.android.smailer.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.preference.MultiSelectListPreference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_WHITELIST

/**
 * Conditions settings activity's fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RulesFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_rules)

        requirePreference(PREF_FILTER_PHONE_BLACKLIST).setOnPreferenceClickListener {
            startActivity(Intent(context, PhoneBlacklistActivity::class.java))
            true
        }

        requirePreference(PREF_FILTER_PHONE_WHITELIST).setOnPreferenceClickListener {
            startActivity(Intent(context, PhoneWhitelistActivity::class.java))
            true
        }

        requirePreference(PREF_FILTER_TEXT_BLACKLIST).setOnPreferenceClickListener {
            startActivity(Intent(context, TextBlacklistActivity::class.java))
            true
        }

        requirePreference(PREF_FILTER_TEXT_WHITELIST).setOnPreferenceClickListener {
            startActivity(Intent(context, TextWhitelistActivity::class.java))
            true
        }
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
        when (key) {
            PREF_EMAIL_TRIGGERS ->
                updateTriggersPreferenceView()
            PREF_FILTER_PHONE_BLACKLIST ->
                updatePhoneBlacklistPreferenceView()
            PREF_FILTER_PHONE_WHITELIST ->
                updatePhoneWhitelistPreferenceView()
            PREF_FILTER_TEXT_BLACKLIST ->
                updateTextBlacklistPreferenceView()
            PREF_FILTER_TEXT_WHITELIST ->
                updateTextWhitelistPreferenceView()
        }
        super.onSharedPreferenceChanged(sharedPreferences, key)
    }

    private fun updateTextWhitelistPreferenceView() {
        val preference = requirePreference(PREF_FILTER_TEXT_WHITELIST)
        val value = settings.getCommaSet(preference.key)
        val formatListSummary = formatListSummary(value, R.string.acceptable_words, R.string._any)

        updateSummary(preference, formatListSummary, SUMMARY_STYLE_DEFAULT)
    }

    private fun updateTextBlacklistPreferenceView() {
        val preference = requirePreference(PREF_FILTER_TEXT_BLACKLIST)
        val value = settings.getCommaSet(preference.key)

        val text = formatListSummary(value, R.string.unacceptable_words, R.string._none)

        updateSummary(preference, text, SUMMARY_STYLE_DEFAULT)
    }

    private fun updatePhoneWhitelistPreferenceView() {
        val preference = requirePreference(PREF_FILTER_PHONE_WHITELIST)
        val value = settings.getCommaSet(preference.key)
        val text = formatListSummary(value, R.string.acceptable_phone_numbers, R.string._any)

        updateSummary(preference, text, SUMMARY_STYLE_DEFAULT)
    }

    private fun updatePhoneBlacklistPreferenceView() {
        val preference = requirePreference(PREF_FILTER_PHONE_BLACKLIST)
        val value = settings.getCommaSet(preference.key)
        val text = formatListSummary(value, R.string.unacceptable_phone_numbers, R.string._none)

        updateSummary(preference, text, SUMMARY_STYLE_DEFAULT)
    }

    private fun updateTriggersPreferenceView() {
        val preference = findPreference<MultiSelectListPreference>(PREF_EMAIL_TRIGGERS)!!
        val value = settings.getStringSet(preference.key)

        if (value.isEmpty()) {
            updateSummary(preference, getString(R.string.no_triggers_specified), SUMMARY_STYLE_ACCENTED)
        } else {
            updateSummary(preference, getString(R.string.events_causing_sending_mail), SUMMARY_STYLE_DEFAULT)
        }
    }

    private fun formatListSummary(set: Set<String>, @StringRes patternRes: Int,
                                  @StringRes emptyRes: Int): String {
        return getString(patternRes, if (set.isEmpty()) getString(emptyRes) else set.size)
    }

}