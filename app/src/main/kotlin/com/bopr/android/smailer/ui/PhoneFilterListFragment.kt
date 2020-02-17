package com.bopr.android.smailer.ui

import com.bopr.android.smailer.R

/**
 * Phone filter list activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal abstract class PhoneFilterListFragment : FilterListFragment() {

    override fun createEditItemDialog(text: String?): EditFilterListItemDialogFragment {
        val dialog = EditPhoneDialogFragment()
        dialog.setTitle(if (text == null) R.string.add else R.string.edit)
        dialog.setInitialValue(text)
        return dialog
    }

    override fun getItemText(value: String?): String? {
        return value
    }
}