package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * Phone number whitelist activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneWhitelistActivity : AppActivity() {

    override fun createFragment(): Fragment {
        return PhoneWhitelistFragment()
    }
}