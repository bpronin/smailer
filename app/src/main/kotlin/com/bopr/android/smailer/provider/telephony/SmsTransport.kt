package com.bopr.android.smailer.provider.telephony

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import com.bopr.android.smailer.util.Mockable

/**
 *  Mockable wrapper for [SmsManager] which is final.
 */
@Mockable
class SmsTransport(val context: Context) {

    fun sendMessage(phone: String?, message: String?) {
        context.smsManager.apply {
            sendMultipartTextMessage(phone, null, divideMessage(message), null, null)
        }
    }

    companion object {

        val Context.smsManager: SmsManager
            get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

    }
}
