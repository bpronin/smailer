package com.bopr.android.smailer.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.R

abstract class BaseDialogFragment(private val dialogTag: String) : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog = dialog
        if (dialog == null) {
            val builder = AlertDialog.Builder(requireContext())
                    .setView(onCreateDialogView())
                    .setPositiveButton(R.string.close) { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }
            onBuildDialog(builder)
            dialog = builder.create()

            /* this is to show soft keyboard when dialog is open */
            dialog.getWindow()!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
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

    abstract fun onCreateDialogView(): View

    open fun onBuildDialog(builder: AlertDialog.Builder) {
        /* do nothing */
    }

    fun showDialog(activity: FragmentActivity) {
        show(activity.supportFragmentManager, dialogTag)
    }

}