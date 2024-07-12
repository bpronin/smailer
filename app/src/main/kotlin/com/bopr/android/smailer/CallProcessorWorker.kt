package com.bopr.android.smailer

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bopr.android.smailer.util.parcelize
import com.bopr.android.smailer.util.unparcelize
import org.slf4j.LoggerFactory

class CallProcessorWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val event = unparcelize(inputData.getByteArray(DATA)!!, PhoneEvent::class)

        log.trace("Processing phone event: {}", event)

        CallProcessor(applicationContext).process(event)
        return Result.success()
    }

    companion object {

        private val log = LoggerFactory.getLogger("CallProcessorWorker")
        private const val DATA = "data"

        fun Context.startPhoneEventProcessing(event: PhoneEvent) {
            log.debug("Starting processing phone event")

            val data = Data.Builder()
                .putByteArray(DATA, parcelize(event))
                .build()

            val request = OneTimeWorkRequest.Builder(CallProcessorWorker::class.java)
                .setInputData(data)
                .build()

            WorkManager.getInstance(this).enqueue(request)
        }

    }
}