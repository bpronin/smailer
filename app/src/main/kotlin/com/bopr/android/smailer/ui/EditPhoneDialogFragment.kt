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
import com.bopr.android.smailer.util.ContentUtils.phoneFromIntent
import com.bopr.android.smailer.util.UiUtil.showToast

/**
 * Phone number editor dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EditPhoneDialogFragment : BaseEditDialogFragment<String>("edit_phone_dialog") {

    private lateinit var editText: EditText
    private var initialValue: String? = null

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        val view = inflater.inflate(R.layout.editor_phone, root, false)

        editText = view.findViewById<EditText>(R.id.edit_text_phone).apply {
            setText(initialValue)
        }

        /* custom message view. do not use setMessage() it's ugly */
        view.findViewById<TextView>(R.id.dialog_message).setText(R.string.enter_phone_number)

        view.findViewById<View>(R.id.button_browse_contacts).setOnClickListener {
            val context = requireContext()
            if (checkPermission(context, READ_CONTACTS)) {
                startActivityForResult(createPickContactIntent(), PICK_CONTACT_REQUEST)
            } else {
                showToast(context, R.string.permissions_required_for_operation)
            }
        }

        return view
    }

    override fun setValue(value: String?) {
        initialValue = value
    }

    override fun getValue(): String {
        return editText.text.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (checkPermission(requireContext(), READ_CONTACTS) &&
                requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            editText.setText(phoneFromIntent(requireContext(), intent))
        }
    }

    companion object {

        private const val PICK_CONTACT_REQUEST = 1009
    }
}