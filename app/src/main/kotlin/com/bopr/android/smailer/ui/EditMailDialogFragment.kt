package com.bopr.android.smailer.ui

import android.Manifest.permission.READ_CONTACTS
import android.app.Activity.RESULT_OK
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.createPickContactIntent
import com.bopr.android.smailer.util.emailFromIntent
import com.bopr.android.smailer.util.showSoftKeyboard
import com.bopr.android.smailer.util.showToast

/**
 * Email editor dialog.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class EditMailDialogFragment : BaseEditDialogFragment<String>("edit_recipient_dialog") {

    private lateinit var editText: TextView
    private var initialValue: String? = null
    private val contactPickerLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            onContactPickComplete(result)
        }

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        val view = inflater.inflate(R.layout.editor_email, root, false)

        editText = view.findViewById<EditText>(android.R.id.edit).apply {
            addTextChangedListener(MailTextValidator(this))
            setText(initialValue)
        }
        editText.post { editText.showSoftKeyboard() }

        view.findViewById<TextView>(android.R.id.message).setText(R.string.email_address)

        view.findViewById<View>(R.id.button_browse_contacts).setOnClickListener {
            if (checkPermission(READ_CONTACTS)) {
                contactPickerLauncher.launch(createPickContactIntent())
            } else {
                showToast(R.string.permissions_required_for_operation)
            }
        }

        return view
    }

    private fun onContactPickComplete(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            if (checkPermission(READ_CONTACTS)) {
                editText.text = requireContext().emailFromIntent(result.data)
            } else {
                log.warn("Missing required permission")
            }
        }
    }

    override fun setValue(value: String?) {
        initialValue = value
    }

    override fun getValue(): String? {
        return editText.text?.toString()
    }

    companion object {

        private val log = Logger("ui.EditEmailDialogFragment")
    }
}