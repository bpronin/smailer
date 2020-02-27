package com.bopr.android.smailer.ui

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.R
import kotlin.reflect.KClass

/**
 * Base Activity with default behaviour.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BaseActivity(private val fragmentClass: KClass<out Fragment>) : AppCompatActivity() {

    private var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeButtonEnabled(true)
        setContentView(R.layout.activity_default)

        val fragmentManager = supportFragmentManager

        fragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT)
        if (fragment == null) {
            fragment = fragmentClass.java.newInstance()
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.content, fragment!!, TAG_FRAGMENT)
                    .commit()
        }
    }

    protected fun setHomeButtonEnabled(enabled: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(enabled)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        fragment!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val TAG_FRAGMENT = "activity_fragment"
    }
}