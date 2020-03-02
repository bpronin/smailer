package com.bopr.android.smailer.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.R
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.add(0, R.id.action_debug, Menu.FIRST, R.string.action_debug)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_debug) {
            startActivity(Intent(this, DebugActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
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