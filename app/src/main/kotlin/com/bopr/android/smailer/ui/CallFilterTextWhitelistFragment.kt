package com.bopr.android.smailer.ui

import com.bopr.android.smailer.Database.Companion.TABLE_TEXT_WHITELIST
import com.bopr.android.smailer.util.unescapeRegex

/**
 * Text whitelist fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallFilterTextWhitelistFragment : CallFilterListFragment(TABLE_TEXT_WHITELIST) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditTextDialogFragment()
    }

    override fun getItemTitle(item: String): String {
        return unescapeRegex(item) ?: item
    }

}