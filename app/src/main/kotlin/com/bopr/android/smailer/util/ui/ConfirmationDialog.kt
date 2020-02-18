package com.bopr.android.smailer.util.ui

import android.content.Context

class ConfirmationDialog(context: Context) : InfoDialog(context) {

    init {
        builder.setNegativeButton(android.R.string.cancel, null)
    }
}