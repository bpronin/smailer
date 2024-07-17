package com.bopr.android.smailer.ui

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings

/**
 * Confirmation dialog with "Do not ask again" checkbox.
 */
class ConfirmCheckDialog(
    title: String? = null,
    message: String? = null,
    positiveButtonText: String? = null,
    negativeButtonText: String? = null,
    private val dialogTag: String,
    onPositiveAction: () -> Unit = {},
    private val onClose: () -> Unit = {}
) : ConfirmDialog(
    title,
    message,
    positiveButtonText,
    negativeButtonText,
    onPositiveAction,
    onClose
) {

    private lateinit var checkBox: CheckBox

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        val view = inflater.inflate(R.layout.alert_dialog_view_container, root) as ViewGroup
        checkBox = CheckBox(context).apply {
            setText(R.string.do_not_ask_again)
        }
        view.addView(checkBox)
        return view
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        Settings(requireContext()).update {
            putBoolean(dialogTag, checkBox.isChecked)
        }
    }

    override fun show(activity: FragmentActivity) {
        val settings = Settings(activity)
        if (!settings.getBoolean(dialogTag)) {
            super.show(activity)
        }else{
            onClose()
        }
    }

}