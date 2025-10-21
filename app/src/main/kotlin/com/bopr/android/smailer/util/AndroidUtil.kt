package com.bopr.android.smailer.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter

/**
 * Returns current device name.
 */
val DEVICE_NAME get() = Build.MANUFACTURER.capitalize() + " " + Build.MODEL

/**
 * NOTE: The method must be named exactly "checkPermission" to pass the IDE inspections and lint warnings
 * when checking methods annotated with [androidx.annotation.RequiresPermission].
 *
 * @see <a href="https://stackoverflow.com/questions/36031218/check-android-permissions-in-a-method">here</a>
 */
fun Context.checkPermission(vararg permissions: String): Boolean {
    return permissions.none { permission ->
        checkSelfPermission(this, permission) != PERMISSION_GRANTED
    }
}

fun Fragment.checkPermission(vararg permissions: String): Boolean {
    return requireContext().checkPermission(*permissions)
}

fun Context.readLogcatLog(): File {
    val file = File(filesDir, "logcat.log")
    try {
        val process = Runtime.getRuntime().exec("logcat -d")
        val src = BufferedReader(InputStreamReader(process.inputStream))
        val dst = PrintWriter(FileOutputStream(file))
        var line: String?
        while (src.readLine().also { line = it } != null) {
            dst.println(line)
        }
        src.close()
        dst.close()
    } catch (x: IOException) {
        throw Exception("Cannot get logcat ", x)
    }
    return file
}

@SuppressLint("ObsoleteSdkInt")
fun Context.sendSmsMessage(phone: String?, message: String?) {
    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        getSystemService(SmsManager::class.java)
    } else {
        @Suppress("DEPRECATION")
        SmsManager.getDefault()
    }

    /*
    val sentIntent = PendingIntent.getBroadcast(
        this, 0, Intent("SMS_SENT"),
        PendingIntent.FLAG_IMMUTABLE
    )
    val deliveredIntent = PendingIntent.getBroadcast(
        this, 0, Intent("SMS_DELIVERED"),
        PendingIntent.FLAG_IMMUTABLE
    )
    */

    smsManager.apply {
        sendMultipartTextMessage(phone, null, divideMessage(message), null, null)
    }
}

/*
fun Context.isInIdleMode(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        (getSystemService(POWER_SERVICE) as PowerManager).isDeviceIdleMode
    } else {
        false
    }
}

fun Context.registerIdleModeChangedReceiver(onChanged: (Boolean) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val receiver: BroadcastReceiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent?) {
                onChanged(isInIdleMode())
            }
        }

        registerReceiver(receiver, IntentFilter(ACTION_DEVICE_IDLE_MODE_CHANGED))
    }
}
*/

