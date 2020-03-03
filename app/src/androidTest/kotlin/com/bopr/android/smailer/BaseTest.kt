package com.bopr.android.smailer

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import org.junit.BeforeClass
import java.util.*

/**
 * Base tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BaseTest protected constructor() {

    companion object {

        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            /* cancel all running jobs to prevent interference */
            WorkManager.getInstance().cancelAllWork()

            Locale.setDefault(Locale.US)
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        }
    }
}