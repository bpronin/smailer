package com.bopr.android.smailer.ui

import android.text.Editable
import android.widget.TextView
import com.bopr.android.smailer.util.TextUtil.isValidEmailAddress

/**
 * Checks that [TextView]'s input matches email address format.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EmailTextValidator(view: TextView) : TextValidator(view) {

    override fun isValidInput(view: TextView, editable: Editable, text: String?): Boolean {
        return isValidEmailAddress(text)
    }
}