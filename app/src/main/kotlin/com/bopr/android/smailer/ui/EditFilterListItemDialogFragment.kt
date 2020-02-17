package com.bopr.android.smailer.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity

/**
 * Base filter list item editor dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class EditFilterListItemDialogFragment : DialogFragment() {

    private var onOkClicked: ((String?) -> Unit)? = null
    private var titleRes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog = dialog
        if (dialog == null) {
            val view = createView()
            dialog = AlertDialog.Builder(requireContext())
                    .setTitle(titleRes)
                    .setView(view)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        onOkClicked?.invoke(getValue())
                    }
                    .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }
                    .create()

            /* show soft keyboard when dialog is open */
            dialog.getWindow()!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
        return dialog
    }

    override fun onDestroyView() {
        /* avoiding of disappearing on rotation */
        if (dialog != null && retainInstance) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    protected abstract fun getValue(): String?

    protected abstract fun createTag(): String

    protected abstract fun createView(): View

    fun setTitle(@StringRes titleRes: Int) {
        this.titleRes = titleRes
    }

    fun setOnOkClicked(action: (String?) -> Unit) {
        onOkClicked = action
    }

    fun showDialog(activity: FragmentActivity) {
        show(activity.supportFragmentManager, createTag())
    }
}