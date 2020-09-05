package com.bopr.android.smailer.ui

import android.widget.TextView
import com.bopr.android.smailer.MailFormatter.Companion.PHONE_SEARCH_TAG
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.isValidUrl

/**
 * Checks that [TextView]'s input matches phone search pattern URL format.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneSearchUrlValidator(view: TextView) : TextValidator(view) {

    override fun getErrorMessage(text: String?): String? {
        return when {
            text.isNullOrEmpty() ->
                null
            !isValidUrl(text) ->
                view.context.getString(R.string.invalid_url)
            !text.contains(PHONE_SEARCH_TAG) ->
                view.context.getString(R.string.phone_search_url_must_contain_phone_tag)
            else ->
                null
        }
    }
}