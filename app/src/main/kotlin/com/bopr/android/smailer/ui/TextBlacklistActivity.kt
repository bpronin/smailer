package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * Number blacklist activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TextBlacklistActivity : AppActivity() {

    override fun createFragment(): Fragment {
        return TextBlacklistFragment()
    }
}