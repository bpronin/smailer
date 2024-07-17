package com.bopr.android.smailer.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.DEFAULT_PHONE_SEARCH_URL
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_PHONE_SEARCH_URL
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.updateSummary

/**
 * More options fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MoreOptionsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_more_options)

//        requirePreference("reset_dialogs").onPreferenceClickListener =
//            Preference.OnPreferenceClickListener {
//                onResetDialogs()
//                true
//            }

        requirePreferenceAs<EditTextPreference>(PREF_DEVICE_ALIAS).setOnBindEditTextListener { editText ->
            editText.hint = DEVICE_NAME
        }

        requirePreferenceAs<EditTextPreference>(PREF_PHONE_SEARCH_URL).setOnBindEditTextListener { editText ->
            editText.hint = DEFAULT_PHONE_SEARCH_URL
            editText.addTextChangedListener(PhoneSearchUrlValidator(editText))
        }
    }

//    private fun onResetDialogs() {
//        Settings(requireContext()).update {
//            remove(BATTERY_OPTIMIZATION_DIALOG_TAG)
//        }
//        showToast(R.string.operation_complete)
//    }

    override fun onStart() {
        super.onStart()

        updateDeviceNamePreferenceView()
        updatePhoneSearchUrlPreferenceView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PREF_DEVICE_ALIAS ->
                updateDeviceNamePreferenceView()

            PREF_PHONE_SEARCH_URL ->
                updatePhoneSearchUrlPreferenceView()
        }
    }

    private fun updateDeviceNamePreferenceView() {
        requirePreference(PREF_DEVICE_ALIAS).updateSummary(settings.getDeviceName())
    }

    private fun updatePhoneSearchUrlPreferenceView() {
        requirePreference(PREF_PHONE_SEARCH_URL).updateSummary(settings.getPhoneSearchUrl())
    }
}