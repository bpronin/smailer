package com.bopr.android.smailer.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.ContentUtils.createPickContactEmailIntent
import com.bopr.android.smailer.util.ContentUtils.emailAddressFromIntent

/**
 * Email editor dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EditEmailDialogFragment() : BaseEditDialogFragment<String>("edit_recipient_dialog") {

    private lateinit var editText: TextView
    private var initialValue: String? = null

    override fun onCreateDialogView(): View {
        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(context).inflate(R.layout.editor_email, null, false)

        editText = view.findViewById<EditText>(android.R.id.edit).apply {
            addTextChangedListener(EmailTextValidator(this))
            setText(initialValue)
        }

        view.findViewById<TextView>(android.R.id.message).setText(R.string.email_address)

        view.findViewById<View>(R.id.button_browse_contacts).apply {
            setOnClickListener {
                startActivityForResult(createPickContactEmailIntent(), PICK_CONTACT_REQUEST)
            }
        }

        return view
    }

    override fun getValue(): String? {
        return editText.text.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            editText.text = emailAddressFromIntent(requireContext(), intent!!)
        }
    }

    fun setInitialValue(value: String?) {
        initialValue = value
    }

    companion object {
        private const val PICK_CONTACT_REQUEST = 100

    }
}