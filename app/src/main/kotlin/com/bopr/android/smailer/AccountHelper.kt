package com.bopr.android.smailer

import android.Manifest.permission.GET_ACCOUNTS
import android.accounts.AccountManager
import android.accounts.AccountManager.KEY_ACCOUNT_NAME
import android.app.Activity
import android.content.Context
import androidx.annotation.RequiresPermission
import com.bopr.android.smailer.util.SingletonHolder

class AccountHelper private constructor (context: Context) {

    private val manager = AccountManager.get(context)

    /**
     * Returns primary device google account or null when no accounts registered.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun getPrimaryGoogleAccount() = manager.accounts.firstOrNull()

    /**
     * Returns primary device google account or throws an exception when no accounts registered.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    @Throws(IllegalArgumentException::class)
    fun requirePrimaryGoogleAccount() =
        requireNotNull(getPrimaryGoogleAccount()) { "Primary Google account is not specified" }

    /**
     * Returns google account with specified name or null.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun getGoogleAccount(accountName: String?) = accountName.let {
        manager.getAccountsByType("com.google").find { it.name == accountName }
    }

    /**
     * Returns google account with specified name or throws an exception.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    @Throws(IllegalArgumentException::class)
    fun requireGoogleAccount(accountName: String?) =
        requireNotNull(getGoogleAccount(accountName)) { "Account $accountName does not exist" }

    /**
     * Returns true if account exists.
     */
    @RequiresPermission(GET_ACCOUNTS /* api<=22 */)
    fun isGoogleAccountExists(accountName: String?) = getGoogleAccount(accountName) != null

    /**
     * Gets an auth token for a particular account, prompting the user for credentials if necessary.
     */
    fun requestGoogleAuthToken(
        activity: Activity,
        accountName: String?,
        scopes: Set<String?>,
        onResponse: (String?) -> Unit
    ) {
        manager.getAuthToken(
            getGoogleAccount(accountName),
            "oauth2: " + scopes.joinToString(" "),
            null,
            activity,
            {
                it.result?.run {
                    onResponse(getString(KEY_ACCOUNT_NAME))
                }
            },
            null
        )
    }

    companion object{

        private val singletonHolder = SingletonHolder{ AccountHelper(it) }
        val Context.accounts get() = singletonHolder.getInstance(this)
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
