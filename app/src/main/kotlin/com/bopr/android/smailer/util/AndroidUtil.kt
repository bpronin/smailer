package com.bopr.android.smailer.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
