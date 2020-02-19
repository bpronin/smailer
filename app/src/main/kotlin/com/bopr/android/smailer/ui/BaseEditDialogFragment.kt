package com.bopr.android.smailer.ui

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog

abstract class BaseEditDialogFragment<V>(dialogTag: String) : BaseDialogFragment(dialogTag) {

    @StringRes
    private var title: Int = 0
    private var okClickedAction: ((V?) -> Unit)? = null

    override fun onBuildDialog(builder: AlertDialog.Builder) {
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