package com.bopr.android.smailer.ui

import android.content.Intent
import android.os.Bundle

/**
 * Main application activity. Individual in different build variants.
 *
 * DEBUG BUILD VARIANT
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class MainAppActivity : BaseAppActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleStartupParams(intent)
    }

    private fun handleStartupParams(intent: Intent) {
        intent.getStringExtra("screen")?.let { extra ->
            when (extra) {
                "debug" ->
                    startActivity(Intent(this, DebugActivity::class.java))
            }
        }
    }
}