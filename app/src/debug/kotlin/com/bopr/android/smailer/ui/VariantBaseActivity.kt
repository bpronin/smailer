package com.bopr.android.smailer.ui

import android.app.Activity
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
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
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
                "debug" -> startActivity(DebugActivity::class)
                "remote_control" -> startActivity(RemoteControlActivity::class)
                "rules" -> startActivity(RulesActivity::class)
                "history" -> startActivity(HistoryActivity::class)
                else -> Log.e("", "Invalid target: $it")
            }
        }
    }

    private fun startActivity(klass: KClass<out Activity>) {
        startActivity(Intent(this, klass.java))
    }

    private inner class ActivityMenuProvider : MenuProvider {

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.debug_menu_main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return if (menuItem.itemId == R.id.action_debug) {
                startActivity(DebugActivity::class)
                true
            } else
                false
        }
    }
}
