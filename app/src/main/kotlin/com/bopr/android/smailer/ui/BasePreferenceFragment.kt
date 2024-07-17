package com.bopr.android.smailer.ui

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SeekBarPreference
import androidx.preference.TwoStatePreference
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.sharedPreferencesName

/**
 * Base [PreferenceFragmentCompat] with default behaviour.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BasePreferenceFragment : PreferenceFragmentCompat(),
    OnSharedPreferenceChangeListener {

    protected lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = sharedPreferencesName

        settings = Settings(requireContext())
        settings.registerOnSharedPreferenceChangeListener(this)
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

    override fun onStart() {
        super.onStart()

        /* when settings have changed from somewhere else but this fragment we need to
           forcibly refresh all views to reflect the changes */
        refreshGroupViews(preferenceScreen)
    }

    private fun refreshGroupViews(group: PreferenceGroup) {
        val all = group.sharedPreferences!!.all
        for (i in 0 until group.preferenceCount) {
            group.getPreference(i).apply {
                if (this is PreferenceGroup) {
                    refreshGroupViews(this)
                } else {
                    val value = all[key]
//                    callChangeListener(value)
                    refreshPreferenceView(this, value)
                }
            }
        }
    }

    private fun refreshPreferenceView(preference: Preference, value: Any?) {
        try {
            when (preference) {
                is TwoStatePreference ->
                    preference.isChecked = (value as Boolean?) ?: false

                is EditTextPreference ->
                    preference.text = value as String?

                is ListPreference ->
                    preference.value = value as String?

                is SeekBarPreference ->
                    preference.value = (value as Int?) ?: 0

                is MultiSelectListPreference -> {
                    @Suppress("UNCHECKED_CAST")
                    preference.values = (value as Set<String>?) ?: emptySet()
                }
            }
        } catch (x: Exception) {
            throw IllegalArgumentException("Failed refreshing $preference", x)
        }
    }

    protected fun <T : Preference> requirePreferenceAs(key: CharSequence): T {
        return requireNotNull(findPreference(key))
    }

    protected fun requirePreference(key: CharSequence): Preference {
        return requireNotNull(findPreference(key))
    }

    companion object {
        private const val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"
    }
}