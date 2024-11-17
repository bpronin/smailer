package com.bopr.android.smailer.messenger.mail

import android.content.Context
import com.bopr.android.smailer.R
import com.bopr.android.smailer.provider.battery.BatteryInfo

/**
 * Formats email subject and body for battery events.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class BatteryLevelMailFormatter(
    private val context: Context,
    private val info: BatteryInfo
) : MailFormatter {

    override fun formatSubject(): String? {
        return "[${context.getString(R.string.app_name)}]"
    }

    override fun formatBody(): String? {
        return context.getString(R.string.low_battery_level)
    }

}