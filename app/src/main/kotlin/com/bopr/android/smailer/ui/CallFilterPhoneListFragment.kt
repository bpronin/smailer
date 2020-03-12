package com.bopr.android.smailer.ui

import com.bopr.android.smailer.util.samePhone

/**
 * Base phone number lists fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class CallFilterPhoneListFragment(settingName: String) : CallFilterListFragment(settingName) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditPhoneDialogFragment()
    }

    override fun isSameItem(item1: String, item2: String): Boolean {
        return samePhone(item1, item2)
    }
}