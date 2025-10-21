package com.bopr.android.smailer.ui

import com.bopr.android.smailer.data.Database.Companion.TABLE_TEXT_BLACKLIST
import com.bopr.android.smailer.util.unescapeRegex

/**
 * Text blacklist fragment.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class TextBlacklistFilterFragment : BaseFilterFragment(TABLE_TEXT_BLACKLIST) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditTextDialogFragment()
    }

    override fun getItemTitle(item: String): String {
        return unescapeRegex(item) ?: item
    }

}