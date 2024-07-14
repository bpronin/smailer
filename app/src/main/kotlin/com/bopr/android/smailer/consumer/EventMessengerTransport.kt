package com.bopr.android.smailer.consumer

import android.content.Context
import com.bopr.android.smailer.external.TelegramException
import com.bopr.android.smailer.provider.telephony.PhoneEventInfo

abstract class EventMessengerTransport(val context: Context) {

    abstract fun sendMessageFor(
        event: PhoneEventInfo,
        onSuccess: () -> Unit,
        onError: (error: Exception) -> Unit
    )

}
