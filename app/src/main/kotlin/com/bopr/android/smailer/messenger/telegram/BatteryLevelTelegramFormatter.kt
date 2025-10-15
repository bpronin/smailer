package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.R
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.battery.BatteryData

class BatteryLevelTelegramFormatter(
    private val context: Context,
    event: Event,
    data: BatteryData
) : TelegramFormatter {

    override val format: String = "HTML"

    override fun formatMessage(): String {
        // TODO: implement
        return context.getString(R.string.low_battery_level)
    }

}
