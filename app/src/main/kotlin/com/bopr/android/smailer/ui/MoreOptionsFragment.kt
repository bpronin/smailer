package com.bopr.android.smailer.ui

import android.os.Bundle
import androidx.preference.EditTextPreference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.DEFAULT_PHONE_SEARCH_URL
import com.bopr.android.smailer.Settings.Companion.PREF_DEVICE_ALIAS
import com.bopr.android.smailer.Settings.Companion.PREF_PHONE_SEARCH_URL
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.requirePreferenceAs
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.updateSummary

/**
 * More options fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MoreOptionsFragment : BasePreferenceFragment(R.xml.pref_more_options) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //        requirePreference("reset_dialogs").onPreferenceClickListener =
//            Preference.OnPreferenceClickListener {
//                onResetDialogs()
//                true
//            }

        requirePreferenceAs<EditTextPreference>(PREF_DEVICE_ALIAS).apply {
            setOnBindEditTextListener { editText ->
                editText.hint = DEVICE_NAME
            }

            setOnChangeListener {
                it.updateSummary(settings.getDeviceName())
            }
        }

        requirePreferenceAs<EditTextPreference>(PREF_PHONE_SEARCH_URL).apply {
            setOnBindEditTextListener { editText ->
                editText.hint = DEFAULT_PHONE_SEARCH_URL
                editText.addTextChangedListener(PhoneSearchUrlValidator(editText))
            }

            setOnChangeListener {
                it.updateSummary(settings.getPhoneSearchUrl())
            }
        }

    }

//    private fun onResetDialogs() {
//        Settings(requireContext()).update {
//            remove(BATTERY_OPTIMIZATION_DIALOG_TAG)
//        }
//        showToast(R.string.operation_complete)
//    }

}