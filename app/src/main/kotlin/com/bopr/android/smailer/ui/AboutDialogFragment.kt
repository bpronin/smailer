package com.bopr.android.smailer.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.util.TagFormatter

/**
 * About dialog fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class AboutDialogFragment : DialogFragment() {

    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_about, container, false)
        val versionLabel = view.findViewById<TextView>(R.id.label_message)
        versionLabel.text = formatVersion()
        versionLabel.setOnLongClickListener {
            val info = settings.getReleaseInfo()
            AlertDialog.Builder(requireContext())
                    .setTitle("Release info")
                    .setMessage("Build number: " + info.number + "\nBuild time: " + info.time)
                    .show()
            true
        }
        view.findViewById<View>(R.id.label_open_source).setOnClickListener {
            startActivity(Intent(context, LegalInfoActivity::class.java))
            dismiss()
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun formatVersion(): String {
        return TagFormatter(requireContext())
                .pattern(R.string.app_version)
                .put("version", settings.getReleaseVersion())
                .format()
    }

    companion object {

        @JvmStatic
        fun showAboutDialog(activity: FragmentActivity) {
            AboutDialogFragment().show(activity.supportFragmentManager, "about_dialog")
        }
    }
}