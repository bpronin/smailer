package com.bopr.android.smailer.ui

/**
 * For debug purposes.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class DebugActivity : BaseActivity() {

    override fun createFragment(): DebugFragment {
        return DebugFragment()
    }
}