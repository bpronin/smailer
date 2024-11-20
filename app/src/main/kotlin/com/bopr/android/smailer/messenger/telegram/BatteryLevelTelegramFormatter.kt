package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.R
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.provider.battery.BatteryInfo

class BatteryLevelTelegramFormatter(
    private val context: Context,
    event: Event,
    data: BatteryInfo
) : TelegramFormatter {

    override fun formatMessage(): String {
        // TODO: implement
        return context.getString(R.string.low_battery_level)
    }

}
