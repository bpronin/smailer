package com.bopr.android.smailer.util

import android.accounts.Account
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager

object AndroidUtil {

    /**
     * Method must be named exactly "checkPermission" to pass the IDE inspections and lint warnings
     * when checking methods annotated with [androidx.annotation.RequiresPermission].
     *
     * @see <a href="https://stackoverflow.com/questions/36031218/check-android-permissions-in-a-method">here</a>
     */
    fun checkPermission(context: Context, vararg permissions: String): Boolean {
        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
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

    fun getAccount(context: Context, accountName: String?): Account? {
        //todo: consider invalidate token when account have been removed not on device
        return GoogleAccountManager(context).getAccountByName(accountName)
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
}