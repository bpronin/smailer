package com.bopr.android.smailer.provider.telephony

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class PhoneCallProcessingWorker(
    context: Context,
    workerParams: WorkerParameters
) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        PhoneCallProcessor(applicationContext).processRecords()
        return Result.success()
    }
}