package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bopr.android.smailer.CallProcessorService.Companion.startCallProcessingService
import com.bopr.android.smailer.util.AndroidUtil.deviceName
import org.slf4j.LoggerFactory

class DebugReceiver : BroadcastReceiver() {

    /* use
        adb shell am broadcast -n com.bopr.android.smailer/.DebugReceiver -a <ACTION>
       to send intents to this receiver */

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("Received intent: $intent")

        if (PROCESS_EVENT == intent.action) {
            onProcessSingleEvent(context)
        }
    }

    private fun onProcessSingleEvent(context: Context) {
        val start = System.currentTimeMillis()
        startCallProcessingService(context, PhoneEvent(
                acceptor = deviceName(),
                phone = "5556",
                text = "SMS TEXT",
                isIncoming = true,
                startTime = start,
                endTime = start + 10000))
    }

    companion object {
        private val log = LoggerFactory.getLogger("DebugReceiver")
        private const val PROCESS_EVENT = "PROCESS_EVENT"
    }
}