package com.bopr.android.smailer.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.ConfirmDialog

@SuppressLint("ObsoleteSdkInt")
internal class BackgroundActivityHelper(
    private val activity: FragmentActivity,
    private val onComplete: () -> Unit
) {

    private val systemSettingsActivityLauncher =
        activity.registerForActivityResult(StartActivityForResult()) {
            onComplete()
        }

    /**
     * Check background activity restriction system setting.
     * For legacy devices, check battery optimization setting.
     */
    fun check() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            checkBackgroundRestriction()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkBatteryOptimization()
        } else {
            onComplete()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkBackgroundRestriction() {
        val activityManager = activity.getSystemService<ActivityManager>()!!
        if (activityManager.isBackgroundRestricted) {
            showBackgroundUsageDialog()
        } else {
            onComplete()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun showBackgroundUsageDialog() {
        ConfirmDialog(
            title = activity.getString(R.string.background_activity_restricted_title),
            message = activity.getString(R.string.background_activity_restricted_reason),
            negativeButtonText = activity.getString(R.string.ask_later),
            positiveButtonText = activity.getString(R.string.settings_title),
            onClose = { confirmed ->
                if (confirmed) {
                    val intent = Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", activity.packageName, null)
                    }
                    systemSettingsActivityLauncher.launch(intent)
                } else {
                    onComplete()
                }
            }
        ).show(activity)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Suppress("DEPRECATION")
    private fun checkBatteryOptimization() {
        val powerManager = activity.getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(activity.packageName)) {
            showOptimizationDialog()
        } else {
            onComplete()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Suppress("DEPRECATION")
    private fun showOptimizationDialog() {
        ConfirmDialog(
            title = activity.getString(R.string.battery_optimization),
            message = activity.getString(R.string.battery_optimization_reason),
            negativeButtonText = activity.getString(R.string.ask_later),
            positiveButtonText = activity.getString(R.string.settings),
            onClose = { confirmed ->
                if (confirmed) {
                    val intent = Intent(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    systemSettingsActivityLauncher.launch(intent)
                } else {
                    onComplete()
                }
            }
        ).show(activity)
    }

}
