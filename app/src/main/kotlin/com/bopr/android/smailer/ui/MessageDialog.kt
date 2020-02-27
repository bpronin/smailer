package com.bopr.android.smailer.ui

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class MessageDialog(private val title: String? = null,
                    private val message: String? = null,
                    private val dismissAction: (() -> Unit)? = null
) : BaseDialogFragment("message-dialog") {

    override fun onBuildDialog(builder: AlertDialog.Builder) {
        super.onBuildDialog(builder)

        builder.setTitle(title)
        builder.setMessage(message)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dismissAction?.invoke()
    }

}