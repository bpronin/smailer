package com.bopr.android.smailer;

import android.accounts.Account;
import android.accounts.OnAccountsUpdateListener;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.bopr.android.smailer.Notifications.ACTION_SHOW_MAIN;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_REMOTE_CONTROL;
import static com.bopr.android.smailer.Settings.PREF_REMOTE_CONTROL_ACCOUNT;
import static com.bopr.android.smailer.Settings.PREF_SENDER_ACCOUNT;

/**
 * Listens for Google accounts changes.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class AccountsObserver implements OnAccountsUpdateListener {

    private static Logger log = LoggerFactory.getLogger("AccountsObserver");

    private final Settings settings;
    private final GoogleAccountManager accountManager;
    private Notifications notifications;

    private AccountsObserver(@NonNull Context context) {
        settings = new Settings(context);
        accountManager = new GoogleAccountManager(context);
        accountManager.getAccountManager().addOnAccountsUpdatedListener(this, null, true);
        notifications = new Notifications(context);
    }

    public static void enable(Context context) {
        new AccountsObserver(context);
    }

    @Override
    public void onAccountsUpdated(Account[] accounts) {
        checkSenderAccount();
        checkServiceAccount();
    }

    private void checkSenderAccount() {
        String accountName = settings.getString(PREF_SENDER_ACCOUNT, null);
        if (accountName != null && accountManager.getAccountByName(accountName) == null) {
            log.warn("Sender account has been removed");

            settings.edit().remove(PREF_SENDER_ACCOUNT).apply();
            notifications.showMessage(R.string.sender_account_removed, ACTION_SHOW_MAIN);
        }
    }

    private void checkServiceAccount() {
        String accountName = settings.getString(PREF_REMOTE_CONTROL_ACCOUNT, null);
        if (accountName != null && accountManager.getAccountByName(accountName) == null) {
            log.warn("Service account has been removed");

            settings.edit().remove(PREF_REMOTE_CONTROL_ACCOUNT).apply();
            notifications.showMessage(R.string.remote_control_account_removed, ACTION_SHOW_REMOTE_CONTROL);
        }
    }

}
