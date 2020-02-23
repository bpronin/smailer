package com.bopr.android.smailer.util;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.MANUFACTURER;
import static android.os.Build.MODEL;
import static com.bopr.android.smailer.util.TextUtil.capitalize;

/**
 * Utilities dependent of android app context.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class AndroidUtil {

    private AndroidUtil() {
    }

    public static boolean checkPermission(@NonNull Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns device name.
     */
    @NonNull
    public static String deviceName() {
        return capitalize(MANUFACTURER) + " " + MODEL;
    }

    /**
     * Returns primary device account.
     */
    @NonNull
    public static Account primaryAccount(@NonNull Context context) {
        return new GoogleAccountManager(context).getAccounts()[0];
    }

    @Nullable
    public static Account getAccount(@NonNull Context context, @Nullable String name) {
        return new GoogleAccountManager(context).getAccountByName(name);
    }
}
