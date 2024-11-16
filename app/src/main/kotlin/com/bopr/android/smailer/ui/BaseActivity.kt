package com.bopr.android.smailer.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.R
import kotlin.reflect.KClass

/**
 * Base application activity with default behaviour.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BaseActivity(private val fragmentClass: KClass<out Fragment>) : AppCompatActivity() {

    private var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeButtonEnabled(true)
        addMenuProvider(ActivityMenuProvider())
        setContentView(R.layout.activity_default)

        val fragmentManager = supportFragmentManager
        fragment = fragmentManager.findFragmentByTag("fragment")
        if (fragment == null) {
            fragment = fragmentClass.java.getDeclaredConstructor().newInstance()
            fragmentManager
                .beginTransaction()
                .replace(R.id.content, fragment!!, "fragment")
                .commit()
        }
    }

    protected fun setHomeButtonEnabled(enabled: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class ActivityMenuProvider : MenuProvider {

        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_main, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return if (menuItem.itemId == R.id.action_about) {
                AboutDialogFragment().show(this@BaseActivity)
                true
            } else
                false
        }
    }

}