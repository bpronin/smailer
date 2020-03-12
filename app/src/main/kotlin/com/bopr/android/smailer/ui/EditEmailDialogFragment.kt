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
import com.bopr.android.smailer.util.*
import org.slf4j.LoggerFactory

/**
 * Email editor dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EditEmailDialogFragment : BaseEditDialogFragment<String>("edit_recipient_dialog") {

    private val log = LoggerFactory.getLogger("EditEmailDialogFragment")
    private lateinit var editText: TextView
    private var initialValue: String? = null

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        val view = inflater.inflate(R.layout.editor_email, root, false)

        editText = view.findViewById<EditText>(android.R.id.edit).apply {
            addTextChangedListener(EmailTextValidator(this))
            setText(initialValue)
        }
        editText.post { editText.showSoftKeyboard() }

        view.findViewById<TextView>(android.R.id.message).setText(R.string.email_address)

        view.findViewById<View>(R.id.button_browse_contacts).setOnClickListener {
            if (checkPermission(READ_CONTACTS)) {
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
        if (checkPermission(READ_CONTACTS)) {
            if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
                editText.text = emailFromIntent(requireContext(), intent)
            }
        } else {
            log.warn("Missing required permission")
        }
    }

    companion object {

        private const val PICK_CONTACT_REQUEST = 1008
    }
}