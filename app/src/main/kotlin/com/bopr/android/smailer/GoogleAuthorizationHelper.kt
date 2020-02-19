package com.bopr.android.smailer

import android.accounts.Account
import android.accounts.AccountManager.KEY_ACCOUNT_NAME
import android.accounts.AccountManager.newChooseAccountIntent
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager.ACCOUNT_TYPE
import org.slf4j.LoggerFactory

/**
 * Convenient class to deal with Google authentication.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class GoogleAuthorizationHelper(private val fragment: Fragment,
                                private val accountSettingName: String,
                                vararg scopes: String?) {

    private val log = LoggerFactory.getLogger("GoogleAuthorizationHelper")
    private val accountManager: GoogleAccountManager
    private val scopes: Collection<String?> = setOf(*scopes)
    private val settings: Settings

    init {
        val context = fragment.requireContext()
        settings = Settings(context)
        accountManager = GoogleAccountManager(context)
    }

    fun isAccountExists(accountName: String?): Boolean {
        return findAccount(accountName) != null
    }

    private fun findAccount(accountName: String?): Account? {
        return accountManager.getAccountByName(accountName)
    }

    private fun selectedAccount(): Account? {
        return findAccount(settings.getString(accountSettingName))
    }

    /* todo: see https://developer.android.com/reference/android/accounts/AccountManager
    public void checkSelectedAccount() {
     try
        makeSomeRequest())
     except
        invalidateToken(accountName);
        requestPermission(accountName);
    }
    */

    /**
     * Brings up system account selection dialog.
     */
    fun selectAccount() {
        val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            newChooseAccountIntent(selectedAccount(), null, arrayOf(ACCOUNT_TYPE), null, null, null, null)
        } else {
            @Suppress("DEPRECATION")
            newChooseAccountIntent(selectedAccount(), null, arrayOf(ACCOUNT_TYPE), false, null, null, null, null)
        }
        fragment.startActivityForResult(intent, REQUEST_ACCOUNT_CHOOSER)
    }

    /**
     * Should be placed to owner fragment or activity onActivityResult().
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ACCOUNT_CHOOSER && resultCode == RESULT_OK && data?.extras != null) {
            val accountName = data.extras!!.getString(KEY_ACCOUNT_NAME)
            requestPermission(accountName)
        }
    }

    private fun requestPermission(accountName: String?) {
        log.debug("Requesting permission for: $accountName")

        accountManager.accountManager.getAuthToken(
                findAccount(accountName),
                "oauth2: " + scopes.joinToString(" "),
                null,
                fragment.requireActivity(),
                AuthTokenAcquireCallback(),
                null /* callback executes in main thread */
        )
    }

    private inner class AuthTokenAcquireCallback : AccountManagerCallback<Bundle?> {

        override fun run(future: AccountManagerFuture<Bundle?>) {
            try {
                val accountName = future.result?.getString("authAccount")
                settings.edit().putString(accountSettingName, accountName).apply()

                log.debug("Selected account: $accountName")
            } catch (x: Exception) {
                log.error("Failed to acquire auth token: ", x)
            }
        }

    }

    companion object {

        private const val REQUEST_ACCOUNT_CHOOSER = 117
    }


}