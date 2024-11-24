package com.bopr.android.smailer.provider.telephony

import android.content.Context
import androidx.work.ListenableWorker.Result.*
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bopr.android.smailer.util.Logger

class PhoneCallProcessWorker(
    context: Context,
    workerParams: WorkerParameters
) :
    Worker(context, workerParams) {

    override fun doWork() = try {
        PhoneCallEventProcessor(applicationContext).process()
        success()
    } catch (x: Throwable) {
        log.error("Work failed", x)

        failure()
    }

    companion object {

        private val log = Logger("PhoneCallProcessWorker")
    }
}