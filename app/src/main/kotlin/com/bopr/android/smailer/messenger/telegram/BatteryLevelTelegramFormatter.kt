package com.bopr.android.smailer.messenger.telegram

import android.content.Context
import com.bopr.android.smailer.R

class BatteryLevelTelegramFormatter(
    private val context: Context,
) : TelegramFormatter {

    override fun formatMessage(): String {
        return context.getString(R.string.low_battery_level)
    }

}
