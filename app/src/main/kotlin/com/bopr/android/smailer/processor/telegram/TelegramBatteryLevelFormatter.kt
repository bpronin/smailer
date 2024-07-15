package com.bopr.android.smailer.processor.telegram

import android.content.Context
import com.bopr.android.smailer.R
import com.bopr.android.smailer.provider.battery.BatteryLevelData

class TelegramBatteryLevelFormatter(
    private val context: Context,
    private val data: BatteryLevelData
) :
    TelegramMessageFormatter(context) {

    override fun formatMessage(): String {
        return context.getString(R.string.low_battery_level)
    }

}
