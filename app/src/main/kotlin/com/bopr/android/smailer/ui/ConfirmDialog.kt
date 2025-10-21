package com.bopr.android.smailer.ui

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

/**
 * Confirmation dialog.
 */
open class ConfirmDialog(
    private val title: String? = null,
    private val message: String? = null,
    private val positiveButtonText: String? = null,
    private val negativeButtonText: String? = null,
    private val onClose: (confirmed: Boolean) -> Unit
) : BaseDialogFragment("confirm-dialog") {

    private var dialogResult = false

    override fun onBuildDialog(builder: AlertDialog.Builder) {
        super.onBuildDialog(builder)

        builder
            .setTitle(title)
            .setMessage(message)
            .setOnDismissListener {
                /**DOES NOT WWORk. See DialogFragment.java:342 */
            }.setPositiveButton(positiveButtonText ?: getString(android.R.string.ok)) { _, _ ->
                dialogResult = true
            }.setNegativeButton(negativeButtonText ?: getString(android.R.string.cancel), null)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onClose(dialogResult)
    }

}