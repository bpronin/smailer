package com.bopr.android.smailer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bopr.android.smailer.AppStartup.startupApplication
import com.bopr.android.smailer.control.mail.MailControlProcessor
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.provider.telephony.PhoneCallEventProcessor
import com.bopr.android.smailer.provider.telephony.PhoneCallEventProcessor.Companion.processPhoneCall
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
                context.processPhoneCall(
                    PhoneCallData(
                        startTime = currentTimeMillis(),
                        phone = "ADB DEBUG",
                        isIncoming = true,
                        text = "Message text"
                    )
                )
            }

            "PROCESS_PENDING_EVENTS" -> {
                runInBackground {
                    PhoneCallEventProcessor(context).process()
                }
            }

            "PROCESS_SERVICE_MAIL" -> {
                MailControlProcessor(context).checkMailbox {}
            }

            "BOOT_COMPLETED" -> { /* we cannot debug BootReceiver directly */
                context.startupApplication()
            }
        }
    }

}