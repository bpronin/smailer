package com.bopr.android.smailer

import android.accounts.Account
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager
import org.slf4j.LoggerFactory

/**
 * Listens for Google accounts changes.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class AccountsObserver(context: Context) : OnAccountsUpdateListener {

    private val settings: Settings = Settings(context)
    private val accountManager: GoogleAccountManager = GoogleAccountManager(context)
    private val notifications: Notifications = Notifications(context)

    init {
        accountManager.accountManager.addOnAccountsUpdatedListener(this, null, true)
    }

    override fun onAccountsUpdated(accounts: Array<Account>) {
        checkSenderAccount()
        checkServiceAccount()
    }

    private fun checkSenderAccount() {
        val accountName = settings.getString(Settings.PREF_SENDER_ACCOUNT, null)
        if (accountName != null && accountManager.getAccountByName(accountName) == null) {
            log.warn("Sender account has been removed")
            settings.edit().remove(Settings.PREF_SENDER_ACCOUNT).apply()
            notifications.showMessage(R.string.sender_account_removed, Notifications.ACTION_SHOW_MAIN)
        }
    }

    private fun checkServiceAccount() {
        val accountName = settings.getString(Settings.PREF_REMOTE_CONTROL_ACCOUNT, null)
        if (accountName != null && accountManager.getAccountByName(accountName) == null) {
            log.warn("Service account has been removed")
            settings.edit().remove(Settings.PREF_REMOTE_CONTROL_ACCOUNT).apply()
            notifications.showMessage(R.string.remote_control_account_removed, Notifications.ACTION_SHOW_REMOTE_CONTROL)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger("AccountsObserver")

        fun enable(context: Context) {
            AccountsObserver(context)
        }
    }

}