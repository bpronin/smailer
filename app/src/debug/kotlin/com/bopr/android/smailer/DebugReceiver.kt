package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bopr.android.smailer.AppStartup.startUpAppServices
import com.bopr.android.smailer.control.MailControlProcessor
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.provider.telephony.PhoneCallProcessor
import com.bopr.android.smailer.provider.telephony.PhoneCallProcessorWorker.Companion.startPhoneCallProcessing
import com.bopr.android.smailer.util.DEVICE_NAME
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
                context.startPhoneCallProcessing(
                    PhoneCallInfo(
                        phone = "ADB DEBUG",
                        isIncoming = true,
                        startTime = currentTimeMillis(),
                        text = "Message text",
                        acceptor = DEVICE_NAME
                    )
                )
            }

            "PROCESS_PENDING_EVENTS" -> {
                runInBackground {
                    PhoneCallProcessor(context).processRecords()
                }
            }

            "PROCESS_SERVICE_MAIL" -> {
                MailControlProcessor(context).checkMailbox {}
            }

            "BOOT_COMPLETED" -> { /* we cannot debug BootReceiver directly */
                context.startUpAppServices()
            }
        }
    }

}