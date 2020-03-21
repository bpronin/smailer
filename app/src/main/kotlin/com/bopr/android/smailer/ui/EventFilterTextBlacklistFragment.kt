package com.bopr.android.smailer.ui

import com.bopr.android.smailer.Database.Companion.TABLE_TEXT_BLACKLIST
import com.bopr.android.smailer.util.unescapeRegex

/**
 * Text blacklist fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EventFilterTextBlacklistFragment : EventFilterListFragment(TABLE_TEXT_BLACKLIST) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditTextDialogFragment()
    }

    override fun getItemTitle(item: String): String {
        return unescapeRegex(item) ?: item
    }

}