package com.bopr.android.smailer.provider.telephony

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bopr.android.smailer.util.parcelize
import com.bopr.android.smailer.util.unparcelize
import org.slf4j.LoggerFactory

class PhoneEventProcessorWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val info = unparcelize(inputData.getByteArray(DATA)!!, PhoneEventData::class)

        log.debug("Processing phone event: {}", info)

        PhoneEventProcessor(applicationContext).process(info)

        return Result.success()
    }

    companion object {

        private val log = LoggerFactory.getLogger("PhoneEventProcessorWorker")
        private const val DATA = "data"

        fun Context.startPhoneEventProcessing(info: PhoneEventData) {
            log.debug("Start processing")

            val data = Data.Builder()
                .putByteArray(DATA, parcelize(info))
                .build()

            val request = OneTimeWorkRequest.Builder(PhoneEventProcessorWorker::class.java)
                .setInputData(data)
                .build()

            WorkManager.getInstance(this).enqueue(request)
        }

    }
}