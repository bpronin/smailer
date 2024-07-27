package com.bopr.android.smailer.util

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SeekBarPreference
import androidx.preference.TwoStatePreference
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat.create
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_DEFAULT
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_UNDERWIVED

enum class SummaryStyle {
    SUMMARY_STYLE_DEFAULT,
    SUMMARY_STYLE_UNDERWIVED,
    SUMMARY_STYLE_ACCENTED
}

fun <T> Preference.runLongTask(
    onPerform: () -> T,
    onComplete: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    val progress = PreferenceProgress(this).apply { start() }

    runLater(
        onComplete = {
            progress.stop()
            onComplete()
        },
        onSuccess = onSuccess,
        onError = onError,
        onPerform = onPerform
    )
}

fun <T> Preference.runBackgroundTask(
    onPerform: () -> T,
    onComplete: () -> Unit = {},
    onSuccess: (T) -> Unit = {},
    onError: (Throwable) -> Unit = {}
) {
    val progress = PreferenceProgress(this).apply { start() }

    runInBackground(
        onComplete = {
            progress.stop()
            onComplete()
        },
        onSuccess = onSuccess,
        onError = onError,
        onPerform = onPerform
    )
}

class PreferenceProgress(
    private val preference: Preference,
    @DrawableRes progressIconRes: Int = R.drawable.animated_progress
) {

    private var originalIcon: Drawable? = null
    private val progressIcon = create(preference.context, progressIconRes)!!

    fun start() {
        originalIcon = preference.icon
        preference.icon = progressIcon
        progressIcon.start()
    }

    fun stop() {
        progressIcon.stop()
        preference.icon = originalIcon
    }
}

fun Preference.updateSummary(
    text: CharSequence?,
    style: SummaryStyle = SUMMARY_STYLE_DEFAULT
) {
    summary = null  /* cleanup to refresh spannable style */
    summary = when (style) {
        SUMMARY_STYLE_DEFAULT -> text

        SUMMARY_STYLE_UNDERWIVED -> context.underwivedText(text)

        SUMMARY_STYLE_ACCENTED -> context.accentedText(text)
    }
}

fun Preference.updateSummary(
    @StringRes valueRes: Int,
    style: SummaryStyle = SUMMARY_STYLE_DEFAULT
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

            else -> { /* is pure Preference */
                callChangeListener(null)
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

fun MultiSelectListPreference.titles(): List<CharSequence> {
    val result = mutableListOf<CharSequence>()
    for ((index, value) in entryValues.withIndex()) {
        if (values.contains(value)) {
            result.add(entries[index])
        }
    }
    return result
}