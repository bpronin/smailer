package com.bopr.android.smailer.ui

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

/**
 * Base dialog fragment with value editor.
 */
abstract class BaseEditDialogFragment<V>(dialogTag: String) : BaseDialogFragment(dialogTag) {

    @StringRes
    private var title: Int = 0
    private var okClickedAction: ((V?) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        /* this is to force show soft keyboard when dialog is open */
        dialog.window?.setSoftInputMode(SOFT_INPUT_STATE_VISIBLE)

        return dialog
    }

    override fun onBuildDialog(builder: AlertDialog.Builder) {
        super.onBuildDialog(builder)
        builder
                .setTitle(title)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    okClickedAction?.invoke(getValue())
                }
                .setNegativeButton(android.R.string.cancel, null)
    }

    abstract fun setValue(value: V?)

    abstract fun getValue(): V?

    fun setTitle(@StringRes title: Int) {
        this.title = title
    }

    fun setOnOkClicked(action: (V?) -> Unit) {
        okClickedAction = action
    }
}