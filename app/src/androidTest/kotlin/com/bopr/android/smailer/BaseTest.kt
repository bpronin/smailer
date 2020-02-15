package com.bopr.android.smailer

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import org.junit.BeforeClass
import org.junit.runner.RunWith
import java.util.*

/**
 * Base tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@RunWith(AndroidJUnit4::class)
abstract class BaseTest protected constructor() {

    companion object {

        @JvmStatic
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            Locale.setDefault(Locale.US)
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
            System.setProperty("dexmaker.dexcache", targetContext.cacheDir.path)
            /* cancel all running jobs to prevent interference */
            WorkManager.getInstance().cancelAllWork()
        }
    }
}