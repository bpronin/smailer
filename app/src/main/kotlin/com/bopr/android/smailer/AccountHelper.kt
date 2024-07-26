package com.bopr.android.smailer

import android.Manifest.permission.GET_ACCOUNTS
import android.accounts.Account
import android.accounts.AccountManager.KEY_ACCOUNT_NAME
import android.app.Activity
import android.content.Context
import androidx.annotation.RequiresPermission
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager

class AccountHelper(context: Context) {

    private val googleAccountManager = GoogleAccountManager(context)

    /**
     * Returns primary device google account or null when no accounts registered.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun getPrimaryGoogleAccount(): Account? {
        return googleAccountManager.accounts.firstOrNull()
    }

    /**
     * Returns primary device google account or throws an exception when no accounts registered.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun requirePrimaryGoogleAccount(): Account {
        return requireNotNull(getPrimaryGoogleAccount())
    }

    /**
     * Returns google account with specified name or null.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun getGoogleAccount(accountName: String?): Account? {
        return googleAccountManager.getAccountByName(accountName)
    }

    /**
     * Returns google account with specified name or throws an exception.
     */
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
        googleAccountManager.accountManager.getAuthToken(
            getGoogleAccount(accountName),
            "oauth2: " + scopes.joinToString(" "),
            null,
            activity,
            { future ->
                future.result?.run {
                    onResponse(getString(KEY_ACCOUNT_NAME))
                }
            },
            null
        )
    }

//    /**
//     * For use in background tasks.
//     */
//    fun requestGoogleAuthTokenSilent(
//        accountName: String?,
//        scopes: Set<String?>,
//        onResponse: (String?) -> Unit
//    ) {
//        val options = Bundle()
//
//        googleAccountManager.accountManager.getAuthToken(
//            getGoogleAccount(accountName),
//            "oauth2: " + scopes.joinToString(" "),
//            options,
//            true,
//            { future ->
//                future.result?.run {
//                    onResponse(getString(KEY_ACCOUNT_NAME))
//                }
//            },
//            null
//        )
//    }
}
