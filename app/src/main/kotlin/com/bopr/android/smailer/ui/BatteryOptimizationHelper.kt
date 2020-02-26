package com.bopr.android.smailer.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.R

internal object BatteryOptimizationHelper {

    const val BATTERY_OPTIMIZATION_DIALOG_TAG = "battery-optimization-do-not-ask-again"

    fun isIgnoreBatteryOptimizationRequired(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = context.applicationContext.packageName
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return !pm.isIgnoringBatteryOptimizations(packageName)
        }
        return false
    }

    fun requireIgnoreBatteryOptimization(activity: FragmentActivity) {
        if (isIgnoreBatteryOptimizationRequired(activity)) {
            showDialog(activity)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun showDialog(activity: FragmentActivity) {
        ConfirmCheckDialog(
                title = activity.getString(R.string.battery_optimization),
                message = activity.getString(R.string.battery_optimization_reason),
                positiveButtonText = activity.getString(R.string.proceed),
                dialogTag = BATTERY_OPTIMIZATION_DIALOG_TAG) {
            activity.startActivity(Intent(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }.show(activity)
    }
}