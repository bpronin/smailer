package com.bopr.android.smailer.provider

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bopr.android.smailer.util.Logger

abstract class EventProcessorWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    abstract fun doProcessEvents()

    override fun doWork() = try {
        doProcessEvents()
        log.debug("Processing finished")
        Result.success()
    } catch (x: Throwable) {
        log.warn("Processing failed", x)
        Result.failure()
    }

    companion object {
        private val log = Logger("EventProcessor")
    }
}