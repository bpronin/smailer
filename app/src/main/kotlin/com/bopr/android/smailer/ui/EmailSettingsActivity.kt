package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * Email message settings activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class EmailSettingsActivity : BaseAppActivity() {

    override fun createFragment(): Fragment {
        return EmailSettingsFragment()
    }
}