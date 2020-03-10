package com.bopr.android.smailer.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


/**
 * Method must be named exactly "checkPermission" to pass the IDE inspections and lint warnings
 * when checking methods annotated with [androidx.annotation.RequiresPermission].
 *
 * @see <a href="https://stackoverflow.com/questions/36031218/check-android-permissions-in-a-method">here</a>
 */
fun Context.checkPermission(vararg permissions: String): Boolean {
    for (p in permissions) {
        if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

fun Fragment.checkPermission(vararg permissions: String): Boolean {
    return requireContext().checkPermission(*permissions)
}

/**
 * Returns device name.
 */
fun deviceName(): String {
    return capitalize(Build.MANUFACTURER) + " " + Build.MODEL
}

fun permissionLabel(context: Context, permissionName: String): String {
    return try {
        val manager = context.packageManager
        val info = manager.getPermissionInfo(permissionName, 0)
        info.loadLabel(manager).toString()
    } catch (x: Exception) {
        throw RuntimeException(x)
    }
}

//fun Context.hasInternetConnection(): Boolean {
//    (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).run {
//        @Suppress("DEPRECATION")
//        return activeNetworkInfo?.isConnectedOrConnecting ?: false
//    }
//}

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
