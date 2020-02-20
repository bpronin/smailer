package com.bopr.android.smailer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.escapeRegex
import com.bopr.android.smailer.util.unescapeRegex

/**
 * Text editor dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EditTextDialogFragment : BaseEditDialogFragment<String>("edit_text_filter_item_dialog") {

    private lateinit var editText: EditText
    private lateinit var checkBox: CheckBox
    private var initialText: String? = null
    private var isRegex = false

    override fun onCreateDialogView(inflater: LayoutInflater, root: ViewGroup?): View {
        val view = inflater.inflate(R.layout.editor_text, root, false)

        editText = view.findViewById<EditText>(R.id.edit_text).apply {
            setText(initialText)
        }

        checkBox = view.findViewById<CheckBox>(R.id.checkbox_regex).apply {
            isChecked = isRegex
        }

        /* custom message view. do not use setMessage() it's ugly */
        view.findViewById<TextView>(R.id.dialog_message).setText(R.string.text_fragment)

        return view
    }

    override fun setValue(value: String?) {
        val regex = unescapeRegex(value)
        isRegex = regex != null
        initialText = if (isRegex) regex else value
    }

    override fun getValue(): String {
        val text = editText.text.toString()
        return if (checkBox.isChecked) escapeRegex(text) else text
    }
}