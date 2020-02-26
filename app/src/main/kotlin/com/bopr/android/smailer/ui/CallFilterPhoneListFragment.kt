package com.bopr.android.smailer.ui

/**
 * Phone filter list activity fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallFilterPhoneListFragment(settingName: String) : CallFilterListFragment(settingName) {

    override fun createEditDialog(): BaseEditDialogFragment<String> {
        return EditPhoneDialogFragment()
    }

}