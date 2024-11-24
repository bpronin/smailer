package com.bopr.android.smailer.ui

import android.os.Bundle
import com.bopr.android.smailer.AppStartup.startupApplication
import com.bopr.android.smailer.PermissionsHelper
import com.bopr.android.smailer.util.requireIgnoreBatteryOptimization

/**
 * Main application activity.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class MainActivity : FlavorBaseActivity(MainFragment::class) {

    private lateinit var permissionsHelper: PermissionsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeButtonEnabled(false)

        permissionsHelper = PermissionsHelper(this)
        permissionsHelper.checkAll()

        requireIgnoreBatteryOptimization()
        startupApplication()
    }

    override fun onDestroy() {
        permissionsHelper.dispose()
        super.onDestroy()
    }

}