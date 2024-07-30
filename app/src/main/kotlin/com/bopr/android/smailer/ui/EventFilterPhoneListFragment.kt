package com.bopr.android.smailer.ui

import com.bopr.android.smailer.R

/**
 * Base phone number lists fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class EventFilterPhoneListFragment(listName: String) : EventFilterListFragment(listName) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditPhoneDialogFragment(R.string.enter_phone_number_or_wildcard)
    }

}