package com.bopr.android.smailer

import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import org.junit.BeforeClass
import java.util.Locale
import java.util.TimeZone


/**
 * Base tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
abstract class BaseTest {

    companion object {

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

        fun getString(resId: Int) = targetContext.getString(resId)

        fun getString(resId: Int, vararg args: Any?) = targetContext.getString(resId, args)

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            /* cancel all running jobs to prevent interference */
            WorkManager.getInstance(targetContext).cancelAllWork()

            Locale.setDefault(Locale.US)
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
        }
    }
}