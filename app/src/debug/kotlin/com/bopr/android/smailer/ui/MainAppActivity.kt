package com.bopr.android.smailer.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

/**
 * Main application activity. Individual in different build variants.
 *
 * DEBUG build variant
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class MainAppActivity(fragmentClass: KClass<out Fragment>) : BaseAppActivity(fragmentClass) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleStartupParams(intent)
    }

    private fun handleStartupParams(intent: Intent) {
        intent.getStringExtra("target")?.let {
            when (it) {
                "debug" ->
                    startActivity(Intent(this, DebugActivity::class.java))
                "remote_control" ->
                    startActivity(Intent(this, RemoteControlActivity::class.java))
                "rules" ->
                    startActivity(Intent(this, RulesActivity::class.java))
                "history" ->
                    startActivity(Intent(this, HistoryActivity::class.java))
                else ->
                    Log.e("", "Invalid target: $it")
            }
        }
    }
}