package com.bopr.android.smailer.ui

import android.widget.TextView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.isValidEmailAddress

/**
 * Checks that [TextView]'s input matches email address format.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EmailTextValidator(view: TextView) : TextValidator(view) {

    override fun getErrorMessage(text: String?): String? {
        return when {
            text.isNullOrEmpty() ->
                null
            !isValidEmailAddress(text) ->
                view.context.getString(R.string.invalid_email)
            else ->
                null
        }
    }
}