package com.bopr.android.smailer.ui

import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_WHITELIST

/**
 * Phone number whitelist fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallFilterPhoneWhitelistFragment : CallFilterListFragment(PREF_FILTER_PHONE_WHITELIST) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditPhoneDialogFragment()
    }

}