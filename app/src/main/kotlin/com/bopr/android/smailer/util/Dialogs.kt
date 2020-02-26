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
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings

object Dialogs {

    const val DIALOG_BATTERY_OPTIMIZATION_NOT_ASK_AGAIN: String = "battery-optimization-dont-ask-again"

    fun resetDialogs(context: Context) {
        Settings(context)
                .edit()
                .remove(DIALOG_BATTERY_OPTIMIZATION_NOT_ASK_AGAIN)
                .apply()
    }

    fun showMessageDialog(context: Context,
                          message: String? = null,
                          closeAction: (() -> Unit)? = null) {
        dialogBuilder(context, null, message)
                .setOnDismissListener { closeAction?.invoke() }
                .show()
    }

    fun showInfoDialog(context: Context,
                       title: String? = null,
                       message: String? = null,
                       buttonText: String? = null,
                       positiveAction: (() -> Unit)? = null) {

        val builder = dialogBuilder(context, title, message)
        buttonText?.let {
            builder.setPositiveButton(buttonText) { _, _ -> positiveAction?.invoke() }
        }
        builder.show()
    }

    fun showConfirmationDialog(context: Context,
                               title: String? = null,
                               message: String? = null,
                               buttonText: String? = null,
                               cancelButtonText: String? = null,
                               dialogTag: String? = null,
                               action: (() -> Unit)? = null) {

        val builder: AlertDialog.Builder

        if (!dialogTag.isNullOrEmpty()) {
            val settings = Settings(context)
            if (settings.getBoolean(dialogTag, false)) {
                return
            }

            val checkBox = CheckBox(context)
            checkBox.setText(R.string.do_not_ask_again)

            @SuppressLint("InflateParams")
            val container = (LayoutInflater.from(context).inflate(
                    R.layout.alert_dialog_view_container, null) as ViewGroup)
            container.addView(checkBox)

            builder = dialogBuilder(context, title, message)
                    .setView(container)
                    .setOnDismissListener {
                        settings.edit().putBoolean(dialogTag, checkBox.isChecked).apply()
                    }
        } else {
            builder = dialogBuilder(context, title, message)
        }

        setPositiveAction(builder, buttonText) { _, _ -> action?.invoke() }
        setNegativeAction(builder, cancelButtonText)

        builder.show()
    }

    fun showInputDialog(context: Context,
                        title: String? = null,
                        message: String? = null,
                        buttonText: String? = null,
                        cancelButtonText: String? = null,
                        inputType: Int = InputType.TYPE_CLASS_TEXT,
                        value: String? = null,
                        action: (String) -> Unit) {

        val builder = dialogBuilder(context, title, message)

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
        setPositiveAction(builder, buttonText) { _, _ -> action(editor.text.toString()) }
        setNegativeAction(builder, cancelButtonText)

        builder.show()
    }

    private fun setPositiveAction(builder: AlertDialog.Builder, buttonText: String?,
                                  callback: (DialogInterface, Int) -> Unit) {
        buttonText?.let {
            builder.setPositiveButton(buttonText, callback)
        } ?: builder.setPositiveButton(android.R.string.ok, callback)
    }

    private fun setNegativeAction(builder: AlertDialog.Builder, buttonText: String?) {
        buttonText?.let {
            builder.setNegativeButton(buttonText, null)
        } ?: builder.setNegativeButton(android.R.string.cancel, null)
    }

    private fun dialogBuilder(context: Context,
                              title: String? = null,
                              message: String? = null
    ): AlertDialog.Builder {

        val builder = AlertDialog.Builder(context)

        title?.let {
            builder.setTitle(it)
        }

        message?.let {
            builder.setMessage(it)
        }

        return builder
    }

}