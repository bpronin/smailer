package com.bopr.android.smailer.ui

import com.bopr.android.smailer.data.Database.Companion.TABLE_TEXT_WHITELIST
import com.bopr.android.smailer.util.unescapeRegex

/**
 * Text whitelist fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EventFilterTextWhitelistFragment : EventFilterListFragment(TABLE_TEXT_WHITELIST) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditTextDialogFragment()
    }

    override fun getItemTitle(item: String): String {
        return unescapeRegex(item) ?: item
    }

}