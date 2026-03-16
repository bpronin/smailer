package com.bopr.android.smailer.ui

import android.os.Bundle
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_BASE_URL
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_MESSENGER_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_PASSWORD
import com.bopr.android.smailer.Settings.Companion.PREF_POCKETBASE_USER
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.messenger.pocketbase.PocketbaseClient
import com.bopr.android.smailer.ui.InfoDialog.Companion.showInfoDialog
import com.bopr.android.smailer.util.PreferenceProgress
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.requirePreferenceAs
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.updateSummary
import kotlinx.coroutines.launch

/**
 * Pocketbase messenger settings fragment.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class PocketbaseSettingsFragment : BasePreferenceFragment(R.xml.pref_pocketbase_settings) {

    private val testSettingsProgress by lazy {
        PreferenceProgress(requirePreference(PREF_TEST_POCKETBASE_CONNECTION))
    }

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

        requirePreference(PREF_TEST_POCKETBASE_CONNECTION).setOnClickListener {
            onTestConnection()
        }
    }

    fun onTestConnection() {
        if (testSettingsProgress.running) return

        val baseUrl = settings.getString(PREF_POCKETBASE_BASE_URL, "")
        val user = settings.getString(PREF_POCKETBASE_USER, "")
        val password = settings.getString(PREF_POCKETBASE_PASSWORD, "")

        lifecycleScope.launch {
            testSettingsProgress.start()
            try {
                PocketbaseClient(baseUrl).auth(user, password)
                showInfoDialog(R.string.test_connection_success)
            } catch (x: Exception) {
                showInfoDialog(R.string.test_connection_failed)
            } finally {
                testSettingsProgress.stop()
            }
        }
    }

    companion object {

        private const val PREF_TEST_POCKETBASE_CONNECTION = "test_pocketbase_connection"
    }
}