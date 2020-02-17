package com.bopr.android.smailer.ui

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.ContentUtils.phoneFromIntent

/**
 * Phone number editor dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EditPhoneDialogFragment : EditFilterListItemDialogFragment() {

    private lateinit var editText: EditText
    private var initialValue: String? = null

    override fun createTag(): String {
        return "edit_phone_dialog"
    }

    override fun getValue(): String {
        return editText.text.toString()
    }

    override fun createView(): View {
        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(context).inflate(R.layout.editor_phone, null, false)

        editText = view.findViewById<EditText>(R.id.edit_text_phone).apply {
            setText(initialValue)
        }

        /* custom message view. do not use setMessage() it's ugly */
        view.findViewById<TextView>(R.id.dialog_message).setText(R.string.enter_phone_number)

        return view
    }

    fun setInitialValue(phone: String?) {
        initialValue = phone
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            editText.setText(phoneFromIntent(requireContext(), intent))
        }
    }

    companion object {
        private const val PICK_CONTACT_REQUEST = 1009
    }
}