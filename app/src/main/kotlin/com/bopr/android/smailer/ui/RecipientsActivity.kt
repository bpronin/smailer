package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * Recipients list activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RecipientsActivity : BaseAppActivity() {

    override fun createFragment(): Fragment {
        return RecipientsFragment()
    }
}