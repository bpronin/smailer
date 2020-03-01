package com.bopr.android.smailer.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings

class ConfirmCheckDialog(title: String? = null,
                         message: String? = null,
                         positiveButtonText: String? = null,
                         negativeButtonText: String? = null,
                         private val dialogTag: String,
                         positiveAction: (() -> Unit)? = null
) : ConfirmDialog(
        title,
        message,
        positiveButtonText,
        negativeButtonText,
        positiveAction) {

    private lateinit var checkBox: CheckBox

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View? {
        checkBox = CheckBox(context)
        checkBox.setText(R.string.do_not_ask_again)

        @SuppressLint("InflateParams")
        val container = (LayoutInflater.from(context).inflate(
                R.layout.alert_dialog_view_container, null) as ViewGroup)
        container.addView(checkBox)
        return container
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Settings(requireContext())
                .edit().putBoolean(dialogTag, checkBox.isChecked).apply()
    }

    override fun show(activity: FragmentActivity) {
        val settings = Settings(activity)
        if (!settings.getBoolean(dialogTag)) {
            super.show(activity)
        }
    }

}