package com.bopr.android.smailer.ui

import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import androidx.preference.EditTextPreference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_BASE_URL
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_PASSWORD
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_USER
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.requirePreferenceAs
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.updateSummary

/**
 * Pocketbase messenger settings fragment.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class PocketbaseSettingsFragment : BasePreferenceFragment(R.xml.pref_pocketbase_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requirePreference(PREF_POCKETBASE_MESSENGER_ENABLED).setOnChangeListener {
            it.apply {
                setTitle(onOffText(settings.getBoolean(it.key)))
            }
        }

        requirePreference(PREF_POCKETBASE_BASE_URL).setOnChangeListener {
            it.apply {
                val value = settings.getString(it.key)
                if (value.isNullOrEmpty()) {
                    updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
                } else {
                    updateSummary(value)
                }
            }
        }

        requirePreference(PREF_POCKETBASE_USER).setOnChangeListener {
            it.apply {
                val value = settings.getString(it.key)
                if (value.isNullOrEmpty()) {
                    updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
                } else {
                    updateSummary(value)
                }
            }
        }

        requirePreferenceAs<EditTextPreference>(PREF_POCKETBASE_PASSWORD).apply {
            setOnBindEditTextListener {
                it.inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
            }

            setOnChangeListener {
                it.apply {
                    val value = settings.getString(it.key)
                    if (value.isNullOrEmpty()) {
                        updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
                    } else {
                        updateSummary(R.string.specified)
                    }
                }
            }
        }
    }
}