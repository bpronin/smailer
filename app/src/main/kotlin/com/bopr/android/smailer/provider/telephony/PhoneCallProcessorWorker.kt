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

class PhoneCallProcessorWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val processor = PhoneCallProcessor(applicationContext)
        val info = unparcelize(inputData.getByteArray(DATA)!!, PhoneCallInfo::class)

        log.debug("Processing phone event: $info")

        try {
            processor.process(info)
            return Result.success()
        } catch (x: Throwable) {
            log.warn("Processing failed. Retrying", x)

            return Result.retry()
        }
    }

    companion object {

        private val log = Logger("PhoneEventProcessorWorker")
        private const val DATA = "data"

        fun Context.startPhoneCallProcessing(info: PhoneCallInfo) {
            log.debug("Start processing")

            val data = Data.Builder()
                .putByteArray(DATA, parcelize(info))
                .build()

            val request = OneTimeWorkRequest.Builder(PhoneCallProcessorWorker::class.java)
                .setInputData(data)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(this).enqueue(request)
        }

    }
}