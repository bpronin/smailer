package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST

/**
 * Phone number blacklist activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallFilterPhoneBlacklistActivity : AppActivity() {

    override fun createFragment(): Fragment {
        return CallFilterPhoneListFragment(PREF_FILTER_PHONE_BLACKLIST)
    }
}