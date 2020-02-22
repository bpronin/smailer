package com.bopr.android.smailer.ui

import android.Manifest.permission
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.bopr.android.smailer.PermissionsHelper.Companion.permissionRationale
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.createPickContactIntent
import com.bopr.android.smailer.util.emailFromIntent
import com.bopr.android.smailer.util.showToast

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
            if (checkPermission(context, permission.READ_CONTACTS)) {
                startActivityForResult(createPickContactIntent(), PICK_CONTACT_REQUEST)
            } else {
                showToast(context, permissionRationale(context, permission.READ_CONTACTS))
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
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            editText.text = emailFromIntent(requireContext(), intent)
        }
    }

    companion object {

        private const val PICK_CONTACT_REQUEST = 1008
    }
}