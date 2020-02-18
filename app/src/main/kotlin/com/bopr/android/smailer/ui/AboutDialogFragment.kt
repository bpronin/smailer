package com.bopr.android.smailer.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.util.TagFormatter
import com.bopr.android.smailer.util.ui.InfoDialog

/**
 * About dialog fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class AboutDialogFragment : BaseDialogFragment("about_dialog") {

    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = Settings(requireContext())
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        val view = inflater.inflate(R.layout.dialog_about, null, false)
        val versionLabel = view.findViewById<TextView>(R.id.label_message)
        versionLabel.text = formatVersion()
        versionLabel.setOnLongClickListener {
            val info = settings.getReleaseInfo()
            InfoDialog(requireContext()).apply {
                setTitle("Release info")
                setMessage("Build number: ${info.number}\nBuild time: ${info.time}")
            }.show()
            true
        }

        view.findViewById<View>(R.id.label_open_source).setOnClickListener {
            startActivity(Intent(context, LegalInfoActivity::class.java))
            dismiss()
        }

        return view
    }

    private fun formatVersion(): String {
        return TagFormatter(requireContext())
                .pattern(R.string.app_version)
                .put("version", settings.getReleaseVersion())
                .format()
    }

}