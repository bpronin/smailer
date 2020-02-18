package com.bopr.android.smailer.ui

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

abstract class BaseEditDialogFragment<R>(dialogTag: String) : BaseDialogFragment(dialogTag) {

    @StringRes
    private var title: Int = 0
    private var okClickedAction: ((R?) -> Unit)? = null

    override fun onBuildDialog(builder: AlertDialog.Builder) {
        builder
                .setTitle(title)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    okClickedAction?.invoke(getValue())
                }
                .setNegativeButton(android.R.string.cancel, null)
    }

    abstract fun getValue(): R?

    fun setTitle(@StringRes title: Int) {
        this.title = title
    }

    fun setOnOkClicked(action: (R?) -> Unit) {
        okClickedAction = action
    }
}