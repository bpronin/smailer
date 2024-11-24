package com.bopr.android.smailer.ui

import android.os.Bundle
import com.bopr.android.smailer.AppStartup.startupApplication
import com.bopr.android.smailer.PermissionsHelper
import com.bopr.android.smailer.util.BatteryOptimizationRequest.Companion.requireIgnoreBatteryOptimization

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
        requireIgnoreBatteryOptimization {
            permissionsHelper.checkAll()
            startupApplication()
        }
    }

    override fun onDestroy() {
        permissionsHelper.dispose()
        super.onDestroy()
    }

}