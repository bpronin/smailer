package com.bopr.android.smailer.ui

import android.accounts.AccountManager.KEY_ACCOUNT_NAME
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.Activity.RESULT_OK
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.util.createPickAccountIntent
import com.bopr.android.smailer.util.getAccount
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import org.slf4j.LoggerFactory

/**
 * Convenient class to deal with Google authentication.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class GoogleAuthorizationHelper(
    fragment: Fragment,
    private val accountSettingName: String,
    vararg scopes: String?
) {

    private val log = LoggerFactory.getLogger("GoogleAuthorizationHelper")
    private val scopes = setOf(*scopes)
    private val activity = fragment.requireActivity()
    private val accountManager = GoogleAccountManager(activity)
    private val settings = Settings(activity)
    private val accountPickerLauncher =
        fragment.registerForActivityResult(StartActivityForResult()) { result ->
            onAccountPickerResult(result)
        }

    /**
     * Brings up system account selection dialog.
     */
    fun startAccountPicker() {
        val account = activity.getAccount(settings.getString(accountSettingName))
        accountPickerLauncher.launch(createPickAccountIntent(account))
    }

    private fun onAccountPickerResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK && result.data?.extras != null) {
            val accountName = result.data!!.extras!!.getString(KEY_ACCOUNT_NAME)
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
            future.result?.run {
                val accountName = getString(KEY_ACCOUNT_NAME)
                settings.update { putString(accountSettingName, accountName) }

                log.debug("Selected account: $accountName")
            }
        }

    }

}