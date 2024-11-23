package com.bopr.android.smailer.ui

import android.accounts.AccountManager.KEY_ACCOUNT_NAME
import android.app.Activity.RESULT_OK
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.AccountHelper.Companion.accounts
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.createPickAccountIntent

/**
 * Convenient class to deal with Google authentication.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class GoogleAuthorizationHelper(
    private val activity: FragmentActivity,
    private val accountSettingName: String,
    vararg scopes: String?
) {

    private val scopes = setOf(*scopes)
    private val settings = activity.settings
    private val accountPickerLauncher =
        activity.registerForActivityResult(StartActivityForResult()) { result ->
            onAccountPickerResult(result)
        }

    /**
     * Brings up system account selection dialog.
     */
    fun startAccountPicker() {
        val account = activity.accounts.getGoogleAccount(settings.getString(accountSettingName))
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

        activity.accounts.requestGoogleAuthToken(
            activity,
            accountName,
            scopes,
            onResponse = { result ->
                settings.update {
                    putString(accountSettingName, result)
                }

                log.debug("Selected account: $result")
            }
        )
    }

    companion object {

        private val log = Logger("ui.GoogleAuthorizationHelper")
    }
}