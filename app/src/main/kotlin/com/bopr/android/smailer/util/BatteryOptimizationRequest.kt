package com.bopr.android.smailer.util

import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.ConfirmCheckDialog

internal class BatteryOptimizationRequest(
    private val activity: FragmentActivity,
    private val onComplete: () -> Unit
) {

    private val activityLauncher =
        activity.registerForActivityResult(StartActivityForResult()) { onComplete() }

    private fun start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onComplete()
        } else activity.run {
            (getSystemService(POWER_SERVICE) as PowerManager).run {
                if (isIgnoringBatteryOptimizations(packageName))
                    onComplete()
                else
                    showDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showDialog() {
        ConfirmCheckDialog(
            title = activity.getString(R.string.battery_optimization),
            message = activity.getString(R.string.battery_optimization_reason),
            negativeButtonText = activity.getString(android.R.string.cancel),
            positiveButtonText = activity.getString(R.string.proceed),
            dialogTag = "battery-optimization-do-not-ask-again",
            onClose = {
                if (it)
                    activityLauncher.launch(Intent(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                else
                    onComplete()
            }
        ).show(activity)
    }

    companion object {

        fun FragmentActivity.requireIgnoreBatteryOptimization(onComplete: () -> Unit = {}) {
            BatteryOptimizationRequest(this, onComplete).start()
        }
    }
}

