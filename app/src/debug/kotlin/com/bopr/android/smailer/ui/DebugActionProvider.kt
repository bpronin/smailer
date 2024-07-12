package com.bopr.android.smailer.ui

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.view.ActionProvider

/**
 * Used in debug menu_main.xml.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class DebugActionProvider (context: Context) : ActionProvider(context) {

    override fun onCreateActionView(): View {
        throw UnsupportedOperationException("DebugActionProvider does ot provide views.")
    }

    override fun onPerformDefaultAction(): Boolean {
        context.startActivity(Intent(context, DebugActivity::class.java))
        return true
    }
}