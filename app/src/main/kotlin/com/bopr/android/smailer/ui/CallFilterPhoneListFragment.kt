package com.bopr.android.smailer.ui

/**
 * Base phone number lists fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class CallFilterPhoneListFragment(listName: String) : CallFilterListFragment(listName) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditPhoneDialogFragment()
    }

}