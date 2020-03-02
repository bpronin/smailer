package com.bopr.android.smailer.util

import android.telephony.SmsManager

/**
 *  Mockable wrapper for [SmsManager] which is final.
 */
@Mockable
class SmsTransport {

    private val manager = SmsManager.getDefault()

    fun sendMessage(phone: String?, message: String?) {
        manager.sendMultipartTextMessage(phone, null, manager.divideMessage(message), null, null)
    }
}
