package com.bopr.android.smailer.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bopr.android.smailer.BuildInfo
import com.bopr.android.smailer.R

/**
 * About dialog fragment.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class AboutDialogFragment : BaseDialogFragment("about_dialog") {

    @SuppressLint("InflateParams")
    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        val view = inflater.inflate(R.layout.dialog_about, root, false)
        val info = BuildInfo.get(requireContext())

        view.findViewById<TextView>(R.id.label_message).apply {
            text = getString(R.string.app_version, info.name)
            setOnLongClickListener {
                showDetails(info)
                true
            }
        }

        view.findViewById<View>(R.id.label_open_source).setOnClickListener {
            startActivity(Intent(context, LegalInfoActivity::class.java))
            dismiss()
        }

        return view
    }

    private fun showDetails(info: BuildInfo) {
        InfoDialog(title = "Release info",
                message = "Build number: ${info.number}\nBuild time: ${info.time}")
                .show(this)
    }

}