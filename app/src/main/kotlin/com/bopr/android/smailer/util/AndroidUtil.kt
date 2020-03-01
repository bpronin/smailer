package com.bopr.android.smailer.util

import android.Manifest.permission.GET_ACCOUNTS
import android.accounts.Account
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
@RequiresPermission(GET_ACCOUNTS)
fun primaryAccount(context: Context): Account? {
    return GoogleAccountManager(context).accounts.getOrNull(0)
}

/**
 * Returns currently selected account.
 */
@RequiresPermission(GET_ACCOUNTS)
fun selectedAccount(context: Context): Account? {
    return getAccount(context, Settings(context).getString(PREF_SENDER_ACCOUNT))
}

/**
 * Returns currently selected service account.
 */
@RequiresPermission(GET_ACCOUNTS)
fun serviceAccount(context: Context): Account? {
    return getAccount(context, Settings(context).getString(PREF_REMOTE_CONTROL_ACCOUNT))
}

/**
 * Returns account with specified name or null.
 */
@RequiresPermission(GET_ACCOUNTS)
fun getAccount(context: Context, accountName: String?): Account? {
    return GoogleAccountManager(context).getAccountByName(accountName)
}