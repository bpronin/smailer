package com.bopr.android.smailer.ui

import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Common info dialog.
 * 
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class InfoDialog(
    private val title: String? = null,
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

    companion object{

        fun Fragment.showInfoDialog(@StringRes messageRes: Int) {
            showInfoDialog(getString(messageRes))
        }

        fun Fragment.showInfoDialog(@StringRes titleRes: Int, @StringRes messageRes: Int) {
            showInfoDialog(getString(titleRes), getString(messageRes))
        }

        fun Fragment.showInfoDialog(message: String) {
            InfoDialog(message = message).show(this)
        }

        fun Fragment.showInfoDialog(title: String, message: String) {
            InfoDialog(title, message).show(this)
        }

        fun FragmentActivity.showInfoDialog(
            title: String? = null,
            message: String,
            positiveButtonText: String? = null,
            positiveAction: (() -> Unit)? = null
        ) {
            InfoDialog(title, message, positiveButtonText, positiveAction).show(this)
        }

        fun FragmentActivity.showInfoDialog(
            @StringRes titleRes: Int? = null,
            @StringRes messageRes: Int,
            positiveButtonText: String? = null,
            positiveAction: (() -> Unit)? = null
        ) {
            showInfoDialog(
                titleRes?.let { getString(it) },
                getString(messageRes),
                positiveButtonText,
                positiveAction
            )
        }
    }
}




