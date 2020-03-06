package com.bopr.android.smailer.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.bopr.android.smailer.R

/**
 * Common dialog with text input field.
 */
class InputDialog(private val title: String? = null,
                  private val message: String? = null,
                  private val inputType: Int = InputType.TYPE_CLASS_TEXT,
                  private val initialValue: String? = null,
                  private val positiveButtonText: String? = null,
                  private val negativeButtonText: String? = null,
                  private val positiveAction: ((String) -> Unit)? = null
) : BaseDialogFragment("input-dialog") {

    private lateinit var editor: EditText

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View? {
        editor = EditText(context)
        editor.inputType = inputType
        editor.requestFocus()
        editor.setText(initialValue)
        editor.setSelection(editor.text.length)

        @SuppressLint("InflateParams")
        val container = (LayoutInflater.from(context).inflate(
                R.layout.alert_dialog_view_container, null) as ViewGroup)
        container.addView(editor)
        return container
    }

    override fun onBuildDialog(builder: AlertDialog.Builder) {
        super.onBuildDialog(builder)

        builder.setTitle(title)
        builder.setMessage(message)

        val positiveText = positiveButtonText ?: getString(android.R.string.ok)
        builder.setPositiveButton(positiveText) { _: DialogInterface, _: Int ->
            positiveAction?.invoke(editor.text.toString())
        }

        val negativeText = negativeButtonText ?: getString(android.R.string.cancel)
        builder.setNegativeButton(negativeText, null)
    }

}