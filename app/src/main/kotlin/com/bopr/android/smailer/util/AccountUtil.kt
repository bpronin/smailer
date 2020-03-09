package com.bopr.android.smailer.util

import android.Manifest.permission.GET_ACCOUNTS
import android.accounts.Account
import android.content.Context
import androidx.annotation.RequiresPermission
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager

/**
 * Returns account with specified name or null.
 */
@RequiresPermission(GET_ACCOUNTS /* api<=22 */)
fun Context.getAccount(accountName: String?): Account? {
    return GoogleAccountManager(this).getAccountByName(accountName)
}

@RequiresPermission(GET_ACCOUNTS /* api<=22 */)
fun Context.isAccountExists(accountName: String?): Boolean {
    return getAccount(accountName) != null
}

/**
 * Returns primary device account or null when no accounts registered.
 */
@RequiresPermission(GET_ACCOUNTS /* api<=22 */)
fun Context.primaryAccount(): Account? {
    return GoogleAccountManager(this).accounts.getOrNull(0)
}
