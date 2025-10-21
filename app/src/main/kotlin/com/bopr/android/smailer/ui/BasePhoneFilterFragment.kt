package com.bopr.android.smailer.ui

import com.bopr.android.smailer.R

/**
 * Base phone number lists fragment.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
abstract class BasePhoneFilterFragment(listName: String) : BaseFilterFragment(listName) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditPhoneDialogFragment(R.string.enter_phone_number_or_wildcard)
    }

}