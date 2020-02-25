package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_WHITELIST

/**
 * Text whitelist activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class CallFilterTextWhitelistActivity : BaseAppActivity() {

    override fun createFragment(): Fragment {
        return CallFilterTextListFragment(PREF_FILTER_TEXT_WHITELIST)
    }
}