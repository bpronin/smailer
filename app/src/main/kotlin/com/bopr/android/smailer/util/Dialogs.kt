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

    fun showConfirmDialog(context: Context,
                          title: String? = null,
                          @StringRes titleRes: Int? = null,
                          message: String? = null,
                          @StringRes messageRes: Int? = null,
                          buttonText: String? = null,
                          @StringRes buttonTextRes: Int? = null,
                          cancelButtonText: String? = null,
                          @StringRes cancelButtonTextRes: Int? = null,
                          action: (() -> Unit)? = null) {
        val builder = dialogBuilder(context, title, titleRes, message, messageRes)

        val function: (DialogInterface, Int) -> Unit = { _, _ -> action?.invoke() }
        buttonText?.let {
            builder.setPositiveButton(buttonText, function)
        } ?: builder.setPositiveButton(buttonTextRes ?: android.R.string.ok, function)

        cancelButtonText?.let {
            builder.setNegativeButton(cancelButtonText, null)
        } ?: builder.setNegativeButton(cancelButtonTextRes ?: android.R.string.cancel, null)

        builder.show()
    }

    fun showConfirmDialogAskAgain(context: Context,
                                  title: String? = null,
                                  @StringRes titleRes: Int? = null,
                                  message: String? = null,
                                  @StringRes messageRes: Int? = null,
                                  buttonText: String? = null,
                                  @StringRes buttonTextRes: Int? = null,
                                  cancelButtonText: String? = null,
                                  @StringRes cancelButtonTextRes: Int? = null,
                                  tag: String?,
                                  action: (() -> Unit)? = null) {

        val builder = dialogBuilder(context, title, titleRes, message, messageRes)

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

            builder.setView(container)
            builder.setOnDismissListener {
                settings.edit().putBoolean(tag, checkBox.isChecked).apply()
            }
        }

        val function: (DialogInterface, Int) -> Unit = { _, _ -> action?.invoke() }
        buttonText?.let {
            builder.setPositiveButton(buttonText, function)
        } ?: builder.setPositiveButton(buttonTextRes ?: android.R.string.ok, function)

        cancelButtonText?.let {
            builder.setNegativeButton(cancelButtonText, null)
        } ?: builder.setNegativeButton(cancelButtonTextRes ?: android.R.string.cancel, null)

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

        val function: (DialogInterface, Int) -> Unit = { _, _ -> action(editor.text.toString()) }
        buttonText?.let {
            builder.setPositiveButton(buttonText, function)
        } ?: builder.setPositiveButton(buttonTextRes ?: android.R.string.ok, function)

        cancelButtonText?.let {
            builder.setNegativeButton(cancelButtonText, null)
        } ?: builder.setNegativeButton(cancelButtonTextRes ?: android.R.string.cancel, null)

        builder.show()
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
