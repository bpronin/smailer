package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * Remote control settings activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class RemoteControlActivity : BaseAppActivity() {

    override fun createFragment(): Fragment {
        return RemoteControlFragment()
    }
}