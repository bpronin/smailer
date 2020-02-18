package com.bopr.android.smailer.util

import android.accounts.Account
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.bopr.android.smailer.util.TextUtil.capitalize
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager

/**
 * Utilities dependent of android app context .
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
object AndroidUtil {

    /**
     * Checks if listed permissions denied.
     *
     * @param context     context
     * @param permissions permissions
     * @return true if any of listed permissions denied
     */
    fun isPermissionsDenied(context: Context, vararg permissions: String): Boolean {
        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    /**
     * Returns device name.
     */
    fun deviceName(): String {
        return capitalize(Build.MANUFACTURER) + " " + Build.MODEL
    }

    /**
     * Returns primary device account.
     */
    fun primaryAccount(context: Context): Account {
        return GoogleAccountManager(context).accounts[0]
    }

}