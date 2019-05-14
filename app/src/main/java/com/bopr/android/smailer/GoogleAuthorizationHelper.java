package com.bopr.android.smailer;

import android.accounts.Account;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OnAccountsUpdateListener;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.newChooseAccountIntent;
import static android.app.Activity.RESULT_OK;
import static com.bopr.android.smailer.Notifications.ACTION_SHOW_REMOTE_CONTROL;
import static com.bopr.android.smailer.Notifications.notifications;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.settings;
import static com.bopr.android.smailer.util.Util.join;
import static java.util.Arrays.asList;

/**
 * Convenient class to deal with Google authentication.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class GoogleAuthorizationHelper {

    private static Logger log = LoggerFactory.getLogger("GoogleAuthorizationHelper");

    private static final int REQUEST_ACCOUNT_CHOOSER = 117;

    private final Fragment fragment;
    private final Settings settings;
    private final GoogleAccountManager accountManager;
    private final String accountSetting;
    private final OnAccountsChangedListener accountsChangedListener;
    private final Collection<String> scopes;

    public GoogleAuthorizationHelper(Fragment fragment, String accountSetting, String... scopes) {
        if (scopes.length == 0) {
            throw new IllegalArgumentException("Scopes cannot be empty");
        }
        this.fragment = fragment;
        this.scopes = asList(scopes);
        this.accountSetting = accountSetting;
        settings = new Settings(fragment.requireContext());
        accountManager = new GoogleAccountManager(fragment.requireContext());
        accountsChangedListener = new OnAccountsChangedListener();
        accountManager.getAccountManager().addOnAccountsUpdatedListener(accountsChangedListener, null, true);
    }

    public void dismiss() {
        accountManager.getAccountManager().removeOnAccountsUpdatedListener(accountsChangedListener);
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
    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    public void selectAccount() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent = newChooseAccountIntent(getSelectedAccount(),
                    null, new String[]{GoogleAccountManager.ACCOUNT_TYPE}, null, null, null, null);
        } else {
            intent = newChooseAccountIntent(getSelectedAccount(),
                    null, new String[]{GoogleAccountManager.ACCOUNT_TYPE}, false, null, null, null, null);
        }
        fragment.startActivityForResult(intent, REQUEST_ACCOUNT_CHOOSER);
    }

    /**
     * Should be placed to owner fragment or activity onActivityResult().
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ACCOUNT_CHOOSER && resultCode == RESULT_OK
                && data != null && data.getExtras() != null) {
            String accountName = data.getExtras().getString(KEY_ACCOUNT_NAME);
            requestPermission(accountName);
        }
    }

    private void requestPermission(String accountName) {
        log.debug("Requesting permission for: " + accountName);

        Account account = accountManager.getAccountByName(accountName);
        accountManager.getAccountManager().getAuthToken(
                account,
                "oauth2: " + join(" ", scopes),
                null,
                fragment.requireActivity(),
                new AuthTokenAcquireCallback(accountName),
                null  /* callback executing in main thread */
        );
    }

    private Account getSelectedAccount() {
        return accountManager.getAccountByName(loadAccount());
    }

    private String loadAccount() {
        return settings.getString(accountSetting, null);
    }

    private void saveAccount(String account) {
        settings.edit().putString(accountSetting, account).apply();
    }

    @Nullable
    public static Account selectedAccount(Context context) {
        String accountName = settings(context).getString(KEY_PREF_SENDER_ACCOUNT, null);
        return new GoogleAccountManager(context).getAccountByName(accountName);
    }

    @NonNull
    public static Account primaryAccount(Context context) {
        return new GoogleAccountManager(context).getAccounts()[0];
    }

    private class AuthTokenAcquireCallback implements AccountManagerCallback<Bundle> {

        private AuthTokenAcquireCallback(String accountName) {
            this.accountName = accountName;
        }

        private final String accountName;

        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            try {
                future.getResult();
                saveAccount(accountName);

                log.debug("Selected account: " + accountName);
            } catch (AuthenticatorException x) {
                log.error("Permission request failed: ", x);
            } catch (IOException x) {
                log.error("Permission request failed: ", x);
            } catch (OperationCanceledException x) {
                log.warn("Permission request canceled");
            }
        }
    }

    private class OnAccountsChangedListener implements OnAccountsUpdateListener {

        @Override
        public void onAccountsUpdated(Account[] accounts) {
            /* clear setting when account removed*/
            if (loadAccount() != null && getSelectedAccount() == null) {
                saveAccount(null);
                notifications(fragment.requireContext())
                        .showMessage(R.string.remote_control_account_removed, ACTION_SHOW_REMOTE_CONTROL);
                log.warn("Account removed");
            }
        }

    }

}

