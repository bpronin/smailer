package com.bopr.android.smailer.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

abstract class BaseDialogFragment(private val fragmentTag: String) : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog = dialog
        if (dialog == null) {
            val builder = AlertDialog.Builder(requireContext())
            onBuildDialog(builder)
            dialog = builder.create()

            /* this is to show soft keyboard when dialog is open */
            dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
        return dialog
    }

    override fun onDestroyView() {
        /* this avoids disappearing on rotation */
        if (dialog != null && retainInstance) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    open fun onBuildDialog(builder: AlertDialog.Builder) {
        onCreateDialogView(LayoutInflater.from(context), null)?.run {
            builder.setView(this)
        }
    }

    open fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View? {
        return null
    }

    fun show(activity: FragmentActivity) {
        show(activity.supportFragmentManager, fragmentTag)
    }

}