package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * An activity that shows legal info.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class LegalInfoActivity : BaseAppActivity() {

    override fun createFragment(): Fragment {
        return LegalInfoFragment()
    }
}