package com.bopr.android.smailer.ui

import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST

/**
 * Phone number blacklist fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallFilterPhoneBlacklistFragment : CallFilterListFragment(PREF_FILTER_PHONE_BLACKLIST) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditPhoneDialogFragment()
    }

}