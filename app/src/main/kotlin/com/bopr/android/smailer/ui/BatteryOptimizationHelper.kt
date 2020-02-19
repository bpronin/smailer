package com.bopr.android.smailer.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.Dialogs.BATTERY_OPTIMIZATION_NOT_ASK_AGAIN
import com.bopr.android.smailer.util.Dialogs.showConfirmDialogAskAgain

internal object BatteryOptimizationHelper {

    fun isIgnoreBatteryOptimizationRequired(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = context.applicationContext.packageName
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return !pm.isIgnoringBatteryOptimizations(packageName)
        }
        return false
    }

    fun requireIgnoreBatteryOptimization(context: Context) {
        if (isIgnoreBatteryOptimizationRequired(context)) {
            showDialog(context)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun showDialog(context: Context) {
        showConfirmDialogAskAgain(context,
                titleRes = R.string.battery_optimization,
                messageRes = R.string.battery_optimization_reason,
                buttonTextRes = R.string.proceed,
                tag = BATTERY_OPTIMIZATION_NOT_ASK_AGAIN) {
            context.startActivity(Intent(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }
}