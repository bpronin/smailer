package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.IntDef
import androidx.preference.*
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.sharedPreferencesName
import com.bopr.android.smailer.util.accentedText
import com.bopr.android.smailer.util.underwivedText
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * Base [PreferenceFragmentCompat] with default behaviour.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BasePreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = sharedPreferencesName
        setHasOptionsMenu(true)

        settings = Settings(requireContext())
        settings.registerOnSharedPreferenceChangeListener(this)
        updatePreferenceViews()
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) == null) {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_about ->
                AboutDialogFragment().show(this)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        updatePreferenceViews()
    }

    private fun updatePreferenceViews() {
        updateGroupPreferenceViews(preferenceScreen)
    }

    private fun updateGroupPreferenceViews(group: PreferenceGroup) {
        val map = settings.all
        for (i in 0 until group.preferenceCount) {
            val preference = group.getPreference(i)
            if (preference is PreferenceGroup) {
                updateGroupPreferenceViews(preference)
            } else {
                val value = map[preference.key]

                preference.callChangeListener(value)

                try {
                    when (preference) {
                        is EditTextPreference ->
                            preference.text = value as String?
                        is SwitchPreference ->
                            preference.isChecked = value as Boolean
                        is CheckBoxPreference ->
                            preference.isChecked = value as Boolean
                        is ListPreference ->
                            preference.value = value as String?
                        is MultiSelectListPreference -> {
                            @Suppress("UNCHECKED_CAST")
                            preference.values = value as Set<String>
                        }
                    }
                } catch (x: Exception) {
                    throw IllegalArgumentException("Cannot update preference: ${preference.key}.", x)
                }
            }
        }
    }

    /**
     * Updates summary of [Preference].
     *
     * @param value      value
     * @param preference preference
     */
    protected fun updateSummary(preference: Preference, value: CharSequence?, @SummaryStyle style: Int) {
        preference.summary = null  /* clean to refresh spannable style */
        when (style) {
            SUMMARY_STYLE_DEFAULT ->
                preference.summary = value
            SUMMARY_STYLE_UNDERWIVED ->
                preference.summary = requireContext().underwivedText(value)
            SUMMARY_STYLE_ACCENTED ->
                preference.summary = requireContext().accentedText(value)
        }
    }

    protected fun requirePreference(key: CharSequence): Preference {
        return findPreference(key)!!
    }

    @Retention(SOURCE)
    @IntDef(SUMMARY_STYLE_DEFAULT, SUMMARY_STYLE_UNDERWIVED, SUMMARY_STYLE_ACCENTED)
    annotation class SummaryStyle

    companion object {
        const val SUMMARY_STYLE_DEFAULT = 0
        const val SUMMARY_STYLE_UNDERWIVED = 1
        const val SUMMARY_STYLE_ACCENTED = 2

        private const val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"
    }
}