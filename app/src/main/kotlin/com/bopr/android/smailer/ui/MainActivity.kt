package com.bopr.android.smailer.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.Environment.setupEnvironment
import com.bopr.android.smailer.Settings

/**
 * An activity that presents a set of application settings.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Settings(this).loadDefaults()
        setupEnvironment(this)
        setHomeButtonEnabled(false)
        handleStartupParams(intent)
    }

    override fun createFragment(): Fragment {
        return MainFragment()
    }

    private fun handleStartupParams(intent: Intent) {
        val stringExtra = intent.getStringExtra("screen")
        if (stringExtra != null) {
            when (stringExtra) {
                "debug" -> try {
                    startActivity(Intent(this, Class.forName("com.bopr.android.smailer.ui.DebugActivity")))
                } catch (x: ClassNotFoundException) {
                    throw RuntimeException(x)
                }
            }
        }
    }
}