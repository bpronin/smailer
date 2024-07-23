package com.bopr.android.smailer.util

import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SeekBarPreference
import androidx.preference.TwoStatePreference
import kotlin.annotation.AnnotationRetention.SOURCE

@Retention(SOURCE)
@IntDef(SUMMARY_STYLE_DEFAULT, SUMMARY_STYLE_UNDERWIVED, SUMMARY_STYLE_ACCENTED)
annotation class SummaryStyle

const val SUMMARY_STYLE_DEFAULT = 0
const val SUMMARY_STYLE_UNDERWIVED = 1
const val SUMMARY_STYLE_ACCENTED = 2

fun Preference.updateSummary(
    text: CharSequence?,
    @SummaryStyle style: Int = SUMMARY_STYLE_DEFAULT
) {
    summary = null  /* cleanup to refresh spannable style */
    when (style) {
        SUMMARY_STYLE_DEFAULT -> summary = text

        SUMMARY_STYLE_UNDERWIVED -> summary = context.underwivedText(text)

        SUMMARY_STYLE_ACCENTED -> summary = context.accentedText(text)
    }
}

fun Preference.updateSummary(
    @StringRes valueRes: Int,
    @SummaryStyle style: Int = SUMMARY_STYLE_DEFAULT
) {
    updateSummary(context.getString(valueRes), style)
}

fun Preference.refreshView() {
    val preferences = requireNotNull(sharedPreferences)
    try {
        when (this) {
            is TwoStatePreference -> {
                isChecked = preferences.getBoolean(key, false)
                callChangeListener(isChecked)
            }

            is EditTextPreference -> {
                text = preferences.getString(key, null)
                callChangeListener(text)
            }

            is ListPreference -> {
                value = preferences.getString(key, null)
                callChangeListener(value)
            }

            is SeekBarPreference -> {
                value = preferences.getInt(key, 0)
                callChangeListener(value)
            }

            is MultiSelectListPreference -> {
                values = preferences.getStringSet(key, emptySet())
                callChangeListener(values)
            }

            is PreferenceGroup -> {
                for (i in 0 until preferenceCount) {
                    getPreference(i).refreshView()
                }
            }
        }
    } catch (x: Exception) {
        throw RuntimeException("Failed refreshing $this", x)
    }
}

fun PreferenceFragmentCompat.requirePreference(key: CharSequence): Preference {
    return requirePreferenceAs(key)
}

fun <T : Preference> PreferenceFragmentCompat.requirePreferenceAs(key: CharSequence): T {
    return requireNotNull(findPreference(key))
}

fun <T : Preference> T.setOnClickListener(onClick: (T) -> Unit) {

    setOnPreferenceClickListener { preference ->
        @Suppress("UNCHECKED_CAST")
        onClick(preference as T)
        true
    }
}

fun <T : Preference> T.setOnChangeListener(onChange: (T) -> Unit) {

    setOnPreferenceChangeListener { preference, _ ->
        @Suppress("UNCHECKED_CAST")
        onChange(preference as T)
        true
    }
}
