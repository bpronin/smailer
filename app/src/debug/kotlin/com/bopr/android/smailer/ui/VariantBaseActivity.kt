package com.bopr.android.smailer.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.R
import kotlin.reflect.KClass

/**
 * Base application activity specific in different build variants.
 *
 * For DEBUG build variant. With debug features.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
open class VariantBaseActivity(value: KClass<out Fragment>) : BaseActivity(value) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleStartupParams(intent)
        addMenuProvider(ActivityMenuProvider())
    }

    private fun handleStartupParams(intent: Intent) {
        intent.getStringExtra("target")?.let {
            when (it) {
                "debug" ->
                    startDebugActivity()

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

    private fun startDebugActivity() {
        startActivity(Intent(this, DebugActivity::class.java))
    }

    private inner class ActivityMenuProvider : MenuProvider {

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.debug_menu_main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return if (menuItem.itemId == R.id.action_debug) {
                startDebugActivity()
                true
            } else
                false
        }
    }
}
