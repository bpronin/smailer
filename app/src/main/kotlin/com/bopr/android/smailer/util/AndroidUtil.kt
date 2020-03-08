package com.bopr.android.smailer.util

import android.Manifest.permission.GET_ACCOUNTS
import android.accounts.Account
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import android.os.PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager


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

/**
 * Returns primary device account or null when no accounts registered.
 */
@RequiresPermission(GET_ACCOUNTS /* api<=22 */)
fun primaryAccount(context: Context): Account? {
    return GoogleAccountManager(context).accounts.getOrNull(0)
}

/**
 * Returns currently selected account.
 */
@RequiresPermission(GET_ACCOUNTS /* api<=22 */)
fun selectedAccount(context: Context): Account? {
    return getAccount(context, Settings(context).getString(PREF_SENDER_ACCOUNT))
}

/**
 * Returns currently selected service account.
 */
@RequiresPermission(GET_ACCOUNTS /* api<=22 */)
fun serviceAccount(context: Context): Account? {
    return getAccount(context, Settings(context).getString(PREF_REMOTE_CONTROL_ACCOUNT))
}

/**
 * Returns account with specified name or null.
 */
@RequiresPermission(GET_ACCOUNTS /* api<=22 */)
fun getAccount(context: Context, accountName: String?): Account? {
    return GoogleAccountManager(context).getAccountByName(accountName)
}

fun Context.hasInternetConnection(): Boolean {
    (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        @Suppress("DEPRECATION")
        return activeNetworkInfo?.isConnectedOrConnecting ?: false
    }
}

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
