package com.bopr.android.smailer.util.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import com.bopr.android.smailer.R

class InputDialog(context: Context) : MessageDialog(context) {

    private val editor: TextView = EditText(context)

    init {
        @SuppressLint("InflateParams")
        val container = (LayoutInflater.from(context)
                .inflate(R.layout.alert_dialog_view_container, null) as ViewGroup).apply {
            addView(editor)
        }
        builder
                .setView(container)
                .setNegativeButton(android.R.string.cancel, null)
    }

    fun setInputType(type: Int) {
        editor.inputType = type
    }

    fun setValue(value: String) {
        editor.text = value
    }

    fun setAction(@StringRes buttonText: Int? = null, action: (String) -> Unit) {
        builder.setPositiveButton(buttonText ?: android.R.string.ok) { _, _ ->
            action(editor.text.toString())
        }
    }

}