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

import com.bopr.android.smailer.util.Util;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.newChooseAccountIntent;
import static android.app.Activity.RESULT_OK;

/*
   To compile debug flavor add SHA-1 fingerprint from <user_dir>/.android/debug.keystore  (password "android") to
   https://console.developers.google.com/apis/credentials/oauthclient/376904884028-f0m6ki37c8b4cf93aktk0jgag3tiu922.apps.googleusercontent.com?project=smailer-24874
*/
public class AuthorizationHelper {

    private static Logger log = LoggerFactory.getLogger("AuthorizationHelper");

    private static final int REQUEST_ACCOUNT_CHOOSER = 117;

    private final Fragment fragment;
    private final Settings settings;
    private final GoogleAccountManager accountManager;
    private final String accountSetting;
    private final OnAccountsChangedListener accountsChangedListener;
    private final List<String> scopes;

    public AuthorizationHelper(Fragment fragment, List<String> scopes, String accountSetting) {
        this.fragment = fragment;
        this.scopes = scopes;
        this.accountSetting = accountSetting;
        settings = new Settings(fragment.requireActivity());
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

    @SuppressWarnings("deprecation")
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
     * Must be placed in fragment's  onActivityResult
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
                "oauth2: " + Util.join(" ", scopes),
                null,
                fragment.requireActivity(),
                new PermissionRequestCallback(accountName),
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
    public static String defaultAccount(Context context) {
        Account[] accounts = new GoogleAccountManager(context).getAccounts();
        return accounts.length > 0 ? accounts[0].name : null;
    }

    private class PermissionRequestCallback implements AccountManagerCallback<Bundle> {

        private PermissionRequestCallback(String accountName) {
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
            if (getSelectedAccount() == null) {
                saveAccount(null);
                log.warn("Account removed");
            }
        }

    }

}

