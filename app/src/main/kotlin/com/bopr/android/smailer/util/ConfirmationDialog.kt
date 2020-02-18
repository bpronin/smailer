package com.bopr.android.smailer.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

class ConfirmationDialog(context: Context) {

    private val builder = AlertDialog.Builder(context)
            .setNegativeButton(android.R.string.cancel, null)

    fun setTitle(@StringRes title: Int): ConfirmationDialog {
        builder.setTitle(title)
        return this
    }

    fun setMessage(@StringRes message: Int): ConfirmationDialog {
        builder.setMessage(message)
        return this
    }

    fun setAction(@StringRes buttonText: Int? = null, action: () -> Unit): ConfirmationDialog {
        builder.setPositiveButton(buttonText ?: android.R.string.ok) { _, _ -> action() }
        return this
    }

    fun show() {
        builder.show()
    }

}