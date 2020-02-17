package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * Text whitelist activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TextWhitelistActivity : AppActivity() {

    override fun createFragment(): Fragment {
        return TextWhitelistFragment()
    }
}