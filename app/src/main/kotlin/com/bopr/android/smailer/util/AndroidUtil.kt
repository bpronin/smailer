package com.bopr.android.smailer.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.ConfirmCheckDialog
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter

/**
 * Returns current device name.
 */
val DEVICE_NAME get() = capitalize(Build.MANUFACTURER) + " " + Build.MODEL

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

fun Context.hasInternetConnection(): Boolean {
    (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        @Suppress("DEPRECATION")
        return activeNetworkInfo?.isConnectedOrConnecting ?: false
    }
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

fun FragmentActivity.requireIgnoreBatteryOptimization(onComplete: () -> Unit = {}): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            ConfirmCheckDialog(
                title = getString(R.string.battery_optimization),
                message = getString(R.string.battery_optimization_reason),
                negativeButtonText = getString(R.string.next_time),
                positiveButtonText = getString(R.string.proceed),
                dialogTag = "battery-optimization-do-not-ask-again",
                onPositiveAction = {
                    startActivity(Intent(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                },
                onClose = onComplete
            ).show(this)

            return true
        }
    }

    onComplete()
    return false
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

