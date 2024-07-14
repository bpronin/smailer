package com.bopr.android.smailer

import android.Manifest.permission.GET_ACCOUNTS
import android.accounts.Account
import android.accounts.AccountManager.KEY_ACCOUNT_NAME
import android.accounts.AccountManagerCallback
import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.RequiresPermission
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager

class AccountManager(context: Context) {

    private val googleAccountManager = GoogleAccountManager(context)

    /**
     * Returns primary device account or null when no accounts registered.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun getPrimaryGoogleAccount(): Account? {
        return googleAccountManager.accounts.firstOrNull()
    }

    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun requirePrimaryGoogleAccount(): Account {
        return googleAccountManager.accounts.first()
    }

    /**
     * Returns account with specified name or null.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun getGoogleAccount(accountName: String?): Account? {
        return googleAccountManager.getAccountByName(accountName)
    }

    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun requireGoogleAccount(accountName: String?): Account {
        return requireNotNull(getGoogleAccount(accountName)) { "Account $accountName does not exist" }
    }

    /**
     * Returns true if account exists.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun isGoogleAccountExists(accountName: String?): Boolean {
        return getGoogleAccount(accountName) != null
    }

    fun requestGoogleAuthToken(
        activity: Activity,
        accountName: String?,
        scopes: Set<String?>,
        onResponse: (String?) -> Unit
    ) {
        val callback = AccountManagerCallback<Bundle?> { future ->
            future.result?.run {
                onResponse(getString(KEY_ACCOUNT_NAME))
            }
        }

        googleAccountManager.accountManager.getAuthToken(
            getGoogleAccount(accountName),
            "oauth2: " + scopes.joinToString(" "),
            null,
            activity,
            callback,
            null
        )
    }
}
