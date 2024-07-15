package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.view.MenuProvider
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SwitchPreference
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
abstract class BasePreferenceFragment : PreferenceFragmentCompat(),
    OnSharedPreferenceChangeListener {

    protected lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = sharedPreferencesName

        settings = Settings(requireContext())
        settings.registerOnSharedPreferenceChangeListener(this)
        updatePreferenceViews()
    }

    override fun onDestroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(FragmentMenuProvider())
    }

    private fun onShowAbout() {
        AboutDialogFragment().show(this)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) == null) {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
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
                    throw IllegalArgumentException(
                        "Cannot update preference: ${preference.key}.",
                        x
                    )
                }
            }
        }
    }

    /**
     * Updates summary of [Preference].
     *
     * @param text      value
     */
    protected fun Preference.updateSummary(
        text: CharSequence?,
        @SummaryStyle style: Int = SUMMARY_STYLE_DEFAULT
    ) {
        summary = null  /* cleanup to refresh spannable style */
        when (style) {
            SUMMARY_STYLE_DEFAULT -> summary = text

            SUMMARY_STYLE_UNDERWIVED -> summary = requireContext().underwivedText(text)

            SUMMARY_STYLE_ACCENTED -> summary = requireContext().accentedText(text)
        }
    }

    protected fun Preference.updateSummary(
        @StringRes valueRes: Int,
        @SummaryStyle style: Int = SUMMARY_STYLE_DEFAULT
    ) {
        updateSummary(getString(valueRes), style)
    }

    protected fun <T : Preference> requirePreferenceAs(key: CharSequence): T {
        return requireNotNull(findPreference(key))
    }

    protected fun requirePreference(key: CharSequence): Preference {
        return requireNotNull(findPreference(key))
    }

    inner class FragmentMenuProvider : MenuProvider {

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return if (menuItem.itemId == R.id.action_about) {
                onShowAbout()
                true
            } else
                false
        }
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