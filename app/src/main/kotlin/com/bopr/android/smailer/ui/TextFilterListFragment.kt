package com.bopr.android.smailer.ui

import com.bopr.android.smailer.util.unescapeRegex

/**
 * Text filter list activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class TextFilterListFragment : CallFilterListFragment() {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditTextDialogFragment()
    }

    override fun getItemTitle(item: String): String {
        return unescapeRegex(item) ?: item
    }

}