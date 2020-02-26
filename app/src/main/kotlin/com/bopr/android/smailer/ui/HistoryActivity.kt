package com.bopr.android.smailer.ui

import androidx.fragment.app.Fragment

/**
 * An activity that presents an application activity log.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class HistoryActivity : BaseAppActivity() {

    override fun createFragment(): Fragment {
        return HistoryFragment()
    }
}