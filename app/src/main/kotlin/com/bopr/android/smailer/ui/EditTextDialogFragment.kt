package com.bopr.android.smailer.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.TextUtil.escapeRegex
import com.bopr.android.smailer.util.TextUtil.unescapeRegex

/**
 * Text editor dialog.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EditTextDialogFragment : EditFilterListItemDialogFragment() {

    private lateinit var editText: EditText
    private lateinit var checkBox: CheckBox
    private var initialText: String? = null
    private var isRegex = false

    override fun createTag(): String {
        return "edit_text_filter_item_dialog"
    }

    override fun getValue(): String {
        val text = editText.text.toString()
        return if (checkBox.isChecked) escapeRegex(text) else text
    }

    override fun createView(): View {
        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(context).inflate(R.layout.editor_text, null, false)

        editText = view.findViewById<EditText>(R.id.edit_text).apply {
            setText(initialText)
        }

        checkBox = view.findViewById<CheckBox>(R.id.checkbox_regex).apply{
            isChecked = isRegex
        }

        /* custom message view. do not use setMessage() it's ugly */
        view.findViewById<TextView>(R.id.dialog_message).setText(R.string.text_fragment)

        return view
    }

    fun setInitialValue(text: String?) {
        val regex = unescapeRegex(text)
        isRegex = regex != null
        initialText = if (isRegex) regex else text
    }
}