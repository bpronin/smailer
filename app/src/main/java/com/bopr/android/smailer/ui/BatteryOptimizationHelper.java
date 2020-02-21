package com.bopr.android.smailer.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import androidx.appcompat.app.AlertDialog;

import com.bopr.android.smailer.R;

import static android.content.Context.POWER_SERVICE;
import static android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS;

class BatteryOptimizationHelper {

    static boolean isIgnoreBatteryOptimizationRequired(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = context.getApplicationContext().getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
            return pm != null && !pm.isIgnoringBatteryOptimizations(packageName);
        }
        return false;
    }

    static void requireIgnoreBatteryOptimization(final Context context) {
        if (isIgnoreBatteryOptimizationRequired(context)) {
            new AlertDialog.Builder(context)
                    .setTitle("Battery optimization")
                    .setMessage(R.string.battery_optimization_reason)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        context.startActivity(intent);
                    })
                    .show();
        }
    }

}
