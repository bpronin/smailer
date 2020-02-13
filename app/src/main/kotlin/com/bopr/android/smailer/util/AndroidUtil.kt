package com.bopr.android.smailer.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.bopr.android.smailer.util.TextUtil.capitalize

/**
 * Utilities dependent of android app context .
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
object AndroidUtil {
    //TODO: remove @JvmStatic

    /**
     * Checks if listed permissions denied.
     *
     * @param context     context
     * @param permissions permissions
     * @return true if any of listed permissions denied
     */
    @JvmStatic
    fun isPermissionsDenied(context: Context, vararg permissions: String): Boolean {
        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    /**
     * Returns denice name.
     */
    @JvmStatic
    fun deviceName(): String {
        return capitalize(Build.MANUFACTURER) + " " + Build.MODEL
    }

}