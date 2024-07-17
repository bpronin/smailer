package com.bopr.android.smailer.util

import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.preference.Preference
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

