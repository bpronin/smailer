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
import androidx.annotation.StringRes
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.createPickContactIntent
import com.bopr.android.smailer.util.phoneFromIntent
import com.bopr.android.smailer.util.showSoftKeyboard
import com.bopr.android.smailer.util.showToast

/**
 * Phone number editor dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EditPhoneDialogFragment(@StringRes private val textRes: Int) :
    BaseEditDialogFragment<String>("edit_phone_dialog") {

    private lateinit var editText: EditText
    private var initialValue: String? = null
    private val contactPickerLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            onContactPickComplete(result)
        }

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        val view = inflater.inflate(R.layout.editor_phone, root, false)

        editText = view.findViewById<EditText>(R.id.edit_text_phone).apply {
            setText(initialValue)
        }
        editText.post { editText.showSoftKeyboard() }

        /* custom message view. do not use setMessage() it's ugly */
        view.findViewById<TextView>(R.id.dialog_message).setText(textRes)

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
                editText.setText(requireContext().phoneFromIntent(result.data))
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

        private val log = Logger("ui.EditPhoneDialogFragment")
    }
}