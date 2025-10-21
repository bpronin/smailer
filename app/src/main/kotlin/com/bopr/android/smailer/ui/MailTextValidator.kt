package com.bopr.android.smailer.ui

import android.widget.TextView
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.isValidEmailAddress

/**
 * Checks that [TextView]'s input matches email address format.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class MailTextValidator(view: TextView) : TextValidator(view) {

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