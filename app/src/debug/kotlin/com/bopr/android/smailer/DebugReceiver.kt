package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bopr.android.smailer.CallProcessorService.Companion.startCallProcessingService
import com.bopr.android.smailer.util.deviceName

class DebugReceiver : BroadcastReceiver() {

    /* use
        adb shell am broadcast -n com.bopr.android.smailer/.DebugReceiver -a <ACTION>
       to send intents to this receiver */

    override fun onReceive(context: Context, intent: Intent) {
        if ("PROCESS_PHONE_EVENT" == intent.action) {
            onProcessPhoneEvent(context)
        }
    }

    private fun onProcessPhoneEvent(context: Context) {
        val start = System.currentTimeMillis()
        startCallProcessingService(context, PhoneEvent(
                phone = "5556",
                isIncoming = true,
                startTime = start,
                endTime = start + 10000,
                text = "SMS TEXT",
                acceptor = deviceName()))
    }

}