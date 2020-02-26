package com.bopr.android.smailer.ui

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

open class ConfirmDialog(private val title: String? = null,
                         private val message: String? = null,
                         private val positiveButtonText: String? = null,
                         private val negativeButtonText: String? = null,
                         private val positiveAction: (() -> Unit)? = null
) : BaseDialogFragment("confirm-dialog") {

    override fun onBuildDialog(builder: AlertDialog.Builder) {
        super.onBuildDialog(builder)

        builder.setTitle(title)
        builder.setMessage(message)

        val positiveText = positiveButtonText ?: getString(android.R.string.ok)
        builder.setPositiveButton(positiveText) { _: DialogInterface, _: Int ->
            positiveAction?.invoke()
        }

        val negativeText = negativeButtonText ?: getString(android.R.string.cancel)
        builder.setNegativeButton(negativeText, null)
    }

}