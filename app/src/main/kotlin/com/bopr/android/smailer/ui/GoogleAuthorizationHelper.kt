package com.bopr.android.smailer.ui

import android.accounts.AccountManager.KEY_ACCOUNT_NAME
import android.accounts.AccountManager.newChooseAccountIntent
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.util.getAccount
import com.bopr.android.smailer.util.showToast
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

    private val accountManager: GoogleAccountManager
    private val activity: Activity = fragment.requireActivity()
    private val scopes: Collection<String?> = setOf(*scopes)
    private val settings: Settings = Settings(activity)

    init {
        accountManager = GoogleAccountManager(activity)
    }

    /**
     * Brings up system account selection dialog.
     */
    fun startAccountSelectorActivity() {
        val selectedAccount = activity.getAccount(settings.getString(accountSettingName))

        val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            newChooseAccountIntent(selectedAccount, null, arrayOf(ACCOUNT_TYPE), null, null, null, null)
        } else {
            @Suppress("DEPRECATION")
            newChooseAccountIntent(selectedAccount, null, arrayOf(ACCOUNT_TYPE), false, null, null, null, null)
        }

        fragment.startActivityForResult(intent, REQUEST_ACCOUNT_CHOOSER)
    }

    /**
     * Should be placed to owner's onActivityResult() method.
     */
    fun onAccountSelectorActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ACCOUNT_CHOOSER && resultCode == RESULT_OK && data?.extras != null) {
            val accountName = data.extras!!.getString(KEY_ACCOUNT_NAME)
            requestPermission(accountName)
        }
    }

    private fun requestPermission(accountName: String?) {
        log.debug("Requesting permission for: $accountName")

        accountManager.accountManager.getAuthToken(
                activity.getAccount(accountName),
                "oauth2: " + scopes.joinToString(" "),
                null,
                activity,
                AuthTokenAcquireCallback(),
                null /* callback executes in main thread */
        )
    }

    private inner class AuthTokenAcquireCallback : AccountManagerCallback<Bundle?> {

        override fun run(future: AccountManagerFuture<Bundle?>) {
            try {
                future.result?.run {
                    val accountName = getString(KEY_ACCOUNT_NAME)
                    settings.update { putString(accountSettingName, accountName) }

                    log.debug("Selected account: $accountName")
                }
            } catch (x: Exception) {
                log.error("Failed to acquire auth token: ", x)
                activity.showToast(R.string.unexpected_error)
            }
        }

    }

    companion object {

        private val log = LoggerFactory.getLogger("GoogleAuthorizationHelper")
        private const val REQUEST_ACCOUNT_CHOOSER = 117
    }

}