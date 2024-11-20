package com.bopr.android.smailer.messenger.mail

import android.content.Context
import com.bopr.android.smailer.R
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.battery.BatteryInfo

/**
 * Formats email subject and body for battery events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BatteryLevelMailFormatter(
    private val context: Context,
    private val event: Event,
    private val info: BatteryInfo
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