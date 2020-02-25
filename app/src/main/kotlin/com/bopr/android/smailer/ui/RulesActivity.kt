package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * Conditions settings activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RulesActivity : BaseAppActivity() {

    override fun createFragment(): Fragment {
        return RulesFragment()
    }
}