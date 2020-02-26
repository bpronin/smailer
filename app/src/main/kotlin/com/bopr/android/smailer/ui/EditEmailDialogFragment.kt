package com.bopr.android.smailer.ui

import android.Manifest.permission.READ_CONTACTS
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.AndroidUtil.checkPermission
import com.bopr.android.smailer.util.ContentUtils.createPickContactIntent
import com.bopr.android.smailer.util.ContentUtils.emailFromIntent
import com.bopr.android.smailer.util.UiUtil.showToast

/**
 * Email editor dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EditEmailDialogFragment : BaseEditDialogFragment<String>("edit_recipient_dialog") {

    private lateinit var editText: TextView
    private var initialValue: String? = null

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        val view = inflater.inflate(R.layout.editor_email, root, false)

        editText = view.findViewById<EditText>(android.R.id.edit).apply {
            addTextChangedListener(EmailTextValidator(this))
            setText(initialValue)
        }

        view.findViewById<TextView>(android.R.id.message).setText(R.string.email_address)

        view.findViewById<View>(R.id.button_browse_contacts).setOnClickListener {
            val context = requireContext()
            if (checkPermission(context, READ_CONTACTS)) {
                startActivityForResult(createPickContactIntent(), PICK_CONTACT_REQUEST)
            } else {
                showToast(R.string.permissions_required_for_operation)
            }
        }

        return view
    }

    override fun setValue(value: String?) {
        initialValue = value
    }

    override fun getValue(): String? {
        return editText.text.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (checkPermission(requireContext(), READ_CONTACTS) &&
                requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            editText.text = emailFromIntent(requireContext(), intent)
        }
    }

    companion object {

        private const val PICK_CONTACT_REQUEST = 1008
    }
}