package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * Options activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class OptionsActivity : BaseAppActivity() {

    override fun createFragment(): Fragment {
        return OptionsFragment()
    }
}