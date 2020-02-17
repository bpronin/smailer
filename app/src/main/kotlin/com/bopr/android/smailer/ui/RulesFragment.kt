package com.bopr.android.smailer.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_WHITELIST
import com.bopr.android.smailer.util.TagFormatter
import com.bopr.android.smailer.util.TextUtil.commaSplit

/**
 * Conditions settings activity's fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RulesFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_rules)

        findPreference<Preference>(PREF_EMAIL_TRIGGERS)!!.apply {
            onPreferenceChangeListener = OnPreferenceChangeListener { preference, value ->
                if (value == null || (value as Set<*>).isEmpty()) {
                    updateSummary(preference, getString(R.string.no_triggers_specified), SUMMARY_STYLE_ACCENTED)
                } else {
                    updateSummary(preference, getString(R.string.events_causing_sending_mail), SUMMARY_STYLE_DEFAULT)
                }
                true
            }
        }

        findPreference<Preference>(PREF_FILTER_PHONE_BLACKLIST)!!.apply {
            onPreferenceClickListener = OnPreferenceClickListener {
                startActivity(Intent(context, PhoneBlacklistActivity::class.java))
                true
            }
            onPreferenceChangeListener = OnPreferenceChangeListener { preference, value ->
                updateSummary(preference, formatSummary(value as String?, R.string.unacceptable_phone_numbers,
                        R.string._none), SUMMARY_STYLE_DEFAULT)
                true
            }
        }

        findPreference<Preference>(PREF_FILTER_PHONE_WHITELIST)!!.apply {
            onPreferenceClickListener = OnPreferenceClickListener {
                startActivity(Intent(context, PhoneWhitelistActivity::class.java))
                true
            }
            onPreferenceChangeListener = OnPreferenceChangeListener { preference, value ->
                updateSummary(preference, formatSummary(value as String?, R.string.acceptable_phone_numbers,
                        R.string._any), SUMMARY_STYLE_DEFAULT)
                true
            }
        }

        findPreference<Preference>(PREF_FILTER_TEXT_BLACKLIST)!!.apply {
            onPreferenceClickListener = OnPreferenceClickListener {
                startActivity(Intent(context, TextBlacklistActivity::class.java))
                true
            }
            onPreferenceChangeListener = OnPreferenceChangeListener { preference, value ->
                updateSummary(preference, formatSummary(value as String?, R.string.unacceptable_words,
                        R.string._none), SUMMARY_STYLE_DEFAULT)
                true
            }
        }

        findPreference<Preference>(PREF_FILTER_TEXT_WHITELIST)!!.apply {
            onPreferenceClickListener = OnPreferenceClickListener {
                startActivity(Intent(context, TextWhitelistActivity::class.java))
                true
            }
            onPreferenceChangeListener = OnPreferenceChangeListener { preference, value ->
                updateSummary(preference, formatSummary(value as String?, R.string.acceptable_words,
                        R.string._any), SUMMARY_STYLE_DEFAULT)
                true
            }
        }
    }

    private fun formatSummary(value: String?, @StringRes patternRes: Int, @StringRes emptyTextRes: Int): String {
        val pattern = TagFormatter(requireContext()).pattern(patternRes)

        if (value.isNullOrBlank()) {
            pattern.put("size", emptyTextRes)
        } else {
            pattern.put("size", commaSplit(value).size.toString())
        }

        return pattern.format()
    }
}