package com.bopr.android.smailer.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import com.bopr.android.smailer.AppStartup.startupApplication
import com.bopr.android.smailer.PermissionsHelper
import com.bopr.android.smailer.util.BackgroundActivityHelper

/**
 * Main application activity.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class MainActivity : FlavorBaseActivity(MainFragment::class) {
    
    private val viewModel: MainViewModel by viewModels()
    private lateinit var permissionsHelper: PermissionsHelper
    private lateinit var backgroundActivityHelper: BackgroundActivityHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeButtonEnabled(false)
        
        permissionsHelper = PermissionsHelper(this)
        backgroundActivityHelper = BackgroundActivityHelper(this, ::onBackgroundActivityCheckComplete)
        
        viewModel.performInitialStartup(this)
    }

    private fun onInitialStartup() {
        backgroundActivityHelper.check()
    }

    private fun onBackgroundActivityCheckComplete() {
        permissionsHelper.checkAll()
        startupApplication()
    }

    override fun onDestroy() {
        permissionsHelper.dispose()
        super.onDestroy()
    }

    /**
     * To perform actions only once at application startup.
     */
    class MainViewModel : ViewModel() {

        private var startupCalled = false

        internal fun performInitialStartup(activity: MainActivity) {
            if (startupCalled) return
            activity.onInitialStartup()
            startupCalled = true
        }
    }
}

