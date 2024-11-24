package com.bopr.android.smailer.control.mail

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.util.Logger

/**
 * Periodically checks service mailbox for messages containing control commands.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class MailControlWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        if (applicationContext.settings.getBoolean(PREF_REMOTE_CONTROL_ENABLED)) {
            log.debug("Working")

            MailControlProcessor(applicationContext).checkMailbox()
        }

        return Result.success()
    }

    internal companion object {

        private val log = Logger("MailControl")
    }
}