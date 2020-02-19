package com.bopr.android.smailer.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.RecipientsFragment.Item
import com.bopr.android.smailer.util.ContentUtils.createPickContactEmailIntent
import com.bopr.android.smailer.util.ContentUtils.emailAddressFromIntent

/**
 * Email editor dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EditRecipientDialogFragment : BaseEditDialogFragment<Item>("edit_recipient_dialog") {

    private lateinit var editText: TextView
    private var initialValue: Item? = null

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        val view = inflater.inflate(R.layout.editor_email, root, false)

        editText = view.findViewById<EditText>(android.R.id.edit).apply {
            addTextChangedListener(EmailTextValidator(this))
            setText(initialValue?.address)
        }

        view.findViewById<TextView>(android.R.id.message).setText(R.string.email_address)

        view.findViewById<View>(R.id.button_browse_contacts).setOnClickListener {
            startActivityForResult(createPickContactEmailIntent(), PICK_CONTACT_REQUEST)
        }

        return view
    }

    override fun setValue(value: Item?) {
        initialValue = value
    }

    override fun getValue(): Item? {
        return Item(editText.text.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            editText.text = emailAddressFromIntent(requireContext(), intent)
        }
    }

    companion object {

        private const val PICK_CONTACT_REQUEST = 1008
    }
}