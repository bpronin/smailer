package com.bopr.android.smailer;

import android.accounts.Account;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

import static android.accounts.AccountManager.KEY_ACCOUNT_NAME;
import static android.accounts.AccountManager.newChooseAccountIntent;
import static android.app.Activity.RESULT_OK;
import static com.bopr.android.smailer.util.TextUtil.join;
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
    private final String settingName;
    private final Collection<String> scopes;

    public GoogleAuthorizationHelper(Fragment fragment, String accountSettingName, String... scopes) {
        if (scopes.length == 0) {
            throw new IllegalArgumentException("Scopes cannot be empty");
        }
        this.fragment = fragment;
        this.scopes = asList(scopes);
        this.settingName = accountSettingName;
        settings = new Settings(fragment.requireContext());
        accountManager = new GoogleAccountManager(fragment.requireContext());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isAccountExists(String accountName) {
        return accountManager.getAccountByName(accountName) != null;
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

    private Account getSelectedAccount() {
        String accountName = settings.getString(settingName, null);
        return accountManager.getAccountByName(accountName);
    }

    private void requestPermission(String accountName) {
        log.debug("Requesting permission for: " + accountName);

        Account account = accountManager.getAccountByName(accountName);
        accountManager.getAccountManager().getAuthToken(
                account,
                "oauth2: " + join(scopes, " "),
                null,
                fragment.requireActivity(),
                new AuthTokenAcquireCallback(accountName),
                null  /* callback executing in main thread */
        );
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
                settings.edit().putString(settingName, accountName).apply();

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

}

