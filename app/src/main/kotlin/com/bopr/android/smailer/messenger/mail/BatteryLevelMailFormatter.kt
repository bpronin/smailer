package com.bopr.android.smailer.messenger.mail

import android.content.Context
import com.bopr.android.smailer.R
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.battery.BatteryData

/**
 * Formats email subject and body for battery events.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
class BatteryLevelMailFormatter(
    private val context: Context,
    private val event: Event,
    private val info: BatteryData
) : MailFormatter {

    override fun formatSubject(): String? {
        // TODO: implement
        return "[${context.getString(R.string.app_name)}]"
    }

    override fun formatBody(): String? {
        // TODO: implement
        return info.text
    }

}