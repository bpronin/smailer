package com.bopr.android.smailer.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * Base dialog fragment.
 */
abstract class BaseDialogFragment(private val fragmentTag: String?) : DialogFragment() {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        retainInstance = true
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var dialog = dialog
        if (dialog == null) {
            val builder = AlertDialog.Builder(requireContext())
            onBuildDialog(builder)
            dialog = builder.create()
        }
        return dialog
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (x: IllegalStateException) {
            /* workaround for issue: https://issuetracker.google.com/issues/36938035 */
        }
    }

    open fun onBuildDialog(builder: AlertDialog.Builder) {
        onCreateDialogView(layoutInflater, null)?.run {
            builder.setView(this)
        }
    }

    open fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View? {
        return null
    }

    open fun show(activity: FragmentActivity) {
        show(activity.supportFragmentManager, fragmentTag)
    }

    open fun show(fragment: Fragment) {
        show(fragment.requireActivity())
    }

}