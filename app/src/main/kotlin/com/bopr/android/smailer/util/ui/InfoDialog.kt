package com.bopr.android.smailer.util.ui

import android.content.Context
import androidx.annotation.StringRes

open class InfoDialog(context: Context) : MessageDialog(context) {

    fun setAction(@StringRes buttonText: Int? = null, action: () -> Unit): InfoDialog {
        builder.setPositiveButton(buttonText ?: android.R.string.ok) { _, _ -> action() }
        return this
    }

}