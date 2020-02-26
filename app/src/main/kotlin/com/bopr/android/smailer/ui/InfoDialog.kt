package com.bopr.android.smailer.ui

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

open class InfoDialog(private val title: String? = null,
                      private val message: String? = null,
                      private val positiveButtonText: String? = null,
                      private val positiveAction: (() -> Unit)? = null
) : BaseDialogFragment("info-dialog") {

    override fun onBuildDialog(builder: AlertDialog.Builder) {
        super.onBuildDialog(builder)

        builder.setTitle(title)
        builder.setMessage(message)

        val positiveText = positiveButtonText ?: getString(android.R.string.ok)
        builder.setPositiveButton(positiveText) { _: DialogInterface, _: Int ->
            positiveAction?.invoke()
        }
    }

}