package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * Phone number blacklist activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PhoneBlacklistActivity : AppActivity() {

    override fun createFragment(): Fragment {
        return PhoneBlacklistFragment()
    }
}