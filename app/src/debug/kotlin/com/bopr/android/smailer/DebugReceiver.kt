package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bopr.android.smailer.CallProcessorWorker.Companion.startPhoneEventProcessing
import com.bopr.android.smailer.Environment.startApplicationServices
import com.bopr.android.smailer.remote.RemoteControlProcessor
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.runInBackground
import java.lang.System.currentTimeMillis

class DebugReceiver : BroadcastReceiver() {

    /* use
        adb shell am broadcast -n com.bopr.android.smailer/.DebugReceiver -a <ACTION>
       to send intents to this receiver */

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "PROCESS_PHONE_EVENT" -> {
                context.startPhoneEventProcessing(PhoneEvent(
                        phone = "ADB DEBUG",
                        isIncoming = true,
                        startTime = currentTimeMillis(),
                        text = "Message text",
                        acceptor = deviceName()))
            }
            "PROCESS_PENDING_EVENTS" -> {
                runInBackground {
                    CallProcessor(context).processPending()
                }
            }
            "PROCESS_SERVICE_MAIL" -> {
                runInBackground {
                    RemoteControlProcessor(context).checkMailbox()
                }
            }
            "BOOT_COMPLETED" -> { /* we cannot debug BootReceiver directly */
                context.startApplicationServices()
            }
        }
    }

}