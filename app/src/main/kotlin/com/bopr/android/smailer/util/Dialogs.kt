package com.bopr.android.smailer.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings

//todo use BaseDialogFragment
object Dialogs {

    const val BATTERY_OPTIMIZATION_NOT_ASK_AGAIN: String = "battery-optimization-dont-ask-again"

    fun resetDialogs(context: Context) {
        Settings(context)
                .edit()
                .remove(BATTERY_OPTIMIZATION_NOT_ASK_AGAIN)
                .apply()
    }

    fun showInfoDialog(context: Context,
                       title: String? = null,
                       @StringRes titleRes: Int? = null,
                       message: String? = null,
                       @StringRes messageRes: Int? = null,
                       buttonText: String? = null,
                       @StringRes buttonTextRes: Int? = null,
                       action: (() -> Unit)? = null) {

        val builder = dialogBuilder(context, title, titleRes, message, messageRes)

        val function: (DialogInterface, Int) -> Unit = { _, _ -> action?.invoke() }
        buttonText?.let {
            builder.setPositiveButton(buttonText, function)
        } ?: buttonTextRes?.let {
            builder.setPositiveButton(buttonTextRes, function)
        }

        builder.show()
    }

    fun showConfirmationDialog(context: Context,
                               title: String? = null,
                               @StringRes titleRes: Int? = null,
                               message: String? = null,
                               @StringRes messageRes: Int? = null,
                               buttonText: String? = null,
                               @StringRes buttonTextRes: Int? = null,
                               cancelButtonText: String? = null,
                               @StringRes cancelButtonTextRes: Int? = null,
                               tag: String? = null,
                               action: (() -> Unit)? = null) {

        val builder: AlertDialog.Builder

        if (!tag.isNullOrEmpty()) {
            val settings = Settings(context)

            if (settings.getBoolean(tag, false)) {
                return
            }

            val checkBox = CheckBox(context)
            checkBox.setText(R.string.do_not_ask_again)

            @SuppressLint("InflateParams")
            val container = (LayoutInflater.from(context).inflate(
                    R.layout.alert_dialog_view_container, null) as ViewGroup)
            container.addView(checkBox)

            builder = dialogBuilder(context, title, titleRes, message, messageRes)
                    .setView(container)
                    .setOnDismissListener {
                        settings.edit().putBoolean(tag, checkBox.isChecked).apply()
                    }
        } else {
            builder = dialogBuilder(context, title, titleRes, message, messageRes)
        }

        setPositiveAction(builder, buttonText, buttonTextRes) { _, _ -> action?.invoke() }
        setNegativeAction(builder, cancelButtonText, cancelButtonTextRes)

        builder.show()
    }

    fun showInputDialog(context: Context,
                        title: String? = null,
                        @StringRes titleRes: Int? = null,
                        message: String? = null,
                        @StringRes messageRes: Int? = null,
                        buttonText: String? = null,
                        @StringRes buttonTextRes: Int? = null,
                        cancelButtonText: String? = null,
                        @StringRes cancelButtonTextRes: Int? = null,
                        inputType: Int = InputType.TYPE_CLASS_TEXT,
                        value: String? = null,
                        action: (String) -> Unit) {

        val builder = dialogBuilder(context, title, titleRes, message, messageRes)

        val editor = EditText(context)
        editor.inputType = inputType
        editor.requestFocus()
        editor.setText(value)
        editor.setSelection(editor.text.length)

        @SuppressLint("InflateParams")
        val container = (LayoutInflater.from(context).inflate(
                R.layout.alert_dialog_view_container, null) as ViewGroup)
        container.addView(editor)

        builder.setView(container)
        setPositiveAction(builder, buttonText, buttonTextRes) { _, _ -> action(editor.text.toString()) }
        setNegativeAction(builder, cancelButtonText, cancelButtonTextRes)

        builder.show()
    }

    private fun setPositiveAction(builder: AlertDialog.Builder, buttonText: String?,
                                  buttonTextRes: Int?, callback: (DialogInterface, Int) -> Unit) {
        buttonText?.let {
            builder.setPositiveButton(buttonText, callback)
        } ?: builder.setPositiveButton(buttonTextRes ?: android.R.string.ok, callback)
    }

    private fun setNegativeAction(builder: AlertDialog.Builder, buttonText: String?, buttonTextRes: Int?) {
        buttonText?.let {
            builder.setNegativeButton(buttonText, null)
        } ?: builder.setNegativeButton(buttonTextRes ?: android.R.string.cancel, null)
    }

    private fun dialogBuilder(context: Context,
                              title: String? = null,
                              @StringRes titleRes: Int? = null,
                              message: String? = null,
                              @StringRes messageRes: Int? = null): AlertDialog.Builder {

        val builder = AlertDialog.Builder(context)

        title?.let {
            builder.setTitle(it)
        } ?: titleRes?.let {
            builder.setTitle(it)
        }

        message?.let {
            builder.setMessage(it)
        } ?: messageRes?.let {
            builder.setMessage(it)
        }

        return builder
    }

}
