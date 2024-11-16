package com.bopr.android.smailer.provider.telephony

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bopr.android.smailer.util.parcelize
import com.bopr.android.smailer.util.unparcelize
import com.bopr.android.smailer.util.Logger
import java.util.concurrent.TimeUnit

class PhoneEventProcessorWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val processor = PhoneEventProcessor(applicationContext)
        val event = unparcelize(inputData.getByteArray(DATA)!!, PhoneEventData::class)

        log.debug("Processing phone event: $event")

        try {
            processor.process(event)
            return Result.success()
        } catch (x: Throwable) {
            log.warn("Processing failed. Retrying", x)

            return Result.retry()
        }
    }

    companion object {

        private val log = Logger("PhoneEventProcessorWorker")
        private const val DATA = "data"

        fun Context.startPhoneEventProcessing(info: PhoneEventData) {
            log.debug("Start processing")

            val data = Data.Builder()
                .putByteArray(DATA, parcelize(info))
                .build()

            val request = OneTimeWorkRequest.Builder(PhoneEventProcessorWorker::class.java)
                .setInputData(data)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(this).enqueue(request)
        }

    }
}