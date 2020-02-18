package com.bopr.android.smailer.util.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

open class MessageDialog(context: Context) {

    protected val builder = AlertDialog.Builder(context)

    fun setTitle(@StringRes title: Int) {
        builder.setTitle(title)
    }

    fun setTitle(title: String) {
        builder.setTitle(title)
    }

    fun setMessage(@StringRes message: Int) {
        builder.setMessage(message)
    }

    fun setMessage(message: String) {
        builder.setMessage(message)
    }

    fun show() {
        builder.show()
    }

}