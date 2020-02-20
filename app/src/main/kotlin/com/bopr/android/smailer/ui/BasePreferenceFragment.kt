package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bopr.android.smailer.PermissionsHelper
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.util.Dialogs.resetDialogs
import com.bopr.android.smailer.util.accentedText
import com.bopr.android.smailer.util.underwivedText

/**
 * Base [PreferenceFragmentCompat] with default behaviour.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BasePreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    lateinit var permissionsHelper: PermissionsHelper
    lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        settings = Settings(requireContext())
        settings.registerOnSharedPreferenceChangeListener(this)
        permissionsHelper = PermissionsHelper(requireActivity())
    }

    override fun onDestroy() {
        permissionsHelper.dispose()
        settings.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (fragmentManager!!.findFragmentByTag(DIALOG_FRAGMENT_TAG) == null) {
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
                AboutDialogFragment().showDialog(requireActivity())
            R.id.action_reset_dialogs ->
                resetDialogs(requireContext())
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        /* nothing by default */
    }

    /**
     * Updates summary of [Preference].
     *
     * @param value      value
     * @param preference preference
     */
    protected fun updateSummary(preference: Preference, value: CharSequence?, style: Int) {
        when (style) {
            SUMMARY_STYLE_DEFAULT ->
                preference.summary = value
            SUMMARY_STYLE_UNDERWIVED ->
                preference.summary = underwivedText(requireContext(), value)
            SUMMARY_STYLE_ACCENTED ->
                preference.summary = accentedText(requireContext(), value)
        }
    }

    protected fun requirePreference(key: CharSequence): Preference {
        return findPreference(key)!!
    }

    companion object {
        const val SUMMARY_STYLE_DEFAULT = 0
        const val SUMMARY_STYLE_UNDERWIVED = 1
        const val SUMMARY_STYLE_ACCENTED = 2

        private const val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"
    }
}