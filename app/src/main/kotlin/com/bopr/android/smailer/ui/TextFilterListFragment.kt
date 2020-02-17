package com.bopr.android.smailer.ui

import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.TextUtil.unescapeRegex

/**
 * Text filter list activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal abstract class TextFilterListFragment : FilterListFragment() {

    override fun createEditItemDialog(text: String?): EditFilterListItemDialogFragment {
        val dialog = EditTextDialogFragment()
        dialog.setTitle(if (text == null) R.string.add else R.string.edit)
        dialog.setInitialValue(text)
        return dialog
    }

    override fun getItemText(value: String?): String? {
        return unescapeRegex(value) ?: value
    }
}