package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bopr.android.smailer.AppStartup.startUpAppServices
import com.bopr.android.smailer.provider.telephony.PhoneEventProcessor
import com.bopr.android.smailer.provider.telephony.PhoneEventProcessorWorker.Companion.startPhoneEventProcessing
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.control.MailControlProcessor
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.runInBackground
import java.lang.System.currentTimeMillis

class DebugReceiver : BroadcastReceiver() {

    /*
        To send intents to this receiver use:
        adb shell am broadcast -n com.bopr.android.smailer/.DebugReceiver -a <ACTION>
    */

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "PROCESS_PHONE_EVENT" -> {
                context.startPhoneEventProcessing(
                    PhoneEventInfo(
                        phone = "ADB DEBUG",
                        isIncoming = true,
                        startTime = currentTimeMillis(),
                        text = "Message text",
                        acceptor = deviceName()
                    )
                )
            }

            "PROCESS_PENDING_EVENTS" -> {
                runInBackground {
                    PhoneEventProcessor(context).processPending()
                }
            }

            "PROCESS_SERVICE_MAIL" -> {
                runInBackground {
                    MailControlProcessor(context).checkMailbox()
                }
            }

            "BOOT_COMPLETED" -> { /* we cannot debug BootReceiver directly */
                context.startUpAppServices()
            }
        }
    }

}