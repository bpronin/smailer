package com.bopr.android.smailer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import androidx.fragment.app.Fragment;

import static com.bopr.android.smailer.Settings.KEY_PREF_SELECTED_ACCOUNT;

/* To compile production add SHA-1 fingerprint from etc\keystore.p12 to
   https://console.developers.google.com/apis/credentials/oauthclient/376904884028-f0m6ki37c8b4cf93aktk0jgag3tiu922.apps.googleusercontent.com?project=smailer-24874
   To compile debug flavor add fingerprint from <user_dir>/.android/debug.keystore  (password "android")
*/
public class AuthorizationHelper {

    private static Logger log = LoggerFactory.getLogger("AuthorizationHelper");

    private static final int REQUEST_ACCOUNT_PICKER = 100;

    private final Settings settings;
    private String scope;
    private GoogleAccountCredential credential;
    private Fragment fragment;

    public AuthorizationHelper(Fragment fragment, String scope) {
        this.fragment = fragment;
        settings = new Settings(fragment.requireActivity());
        this.scope = scope;

        List<String> scopes = Collections.singletonList(scope);
        credential = GoogleAccountCredential.usingOAuth2(fragment.requireContext(), scopes);
        credential.setSelectedAccountName(settings.getString(KEY_PREF_SELECTED_ACCOUNT, ""));
    }

    public GoogleAccountCredential getCredential() {
        return credential;
    }

    public void requestPermission(String accountName) {
        Account account = credential.getGoogleAccountManager().getAccountByName(accountName);
        AccountManager accountManager = credential.getGoogleAccountManager().getAccountManager();
        accountManager.getAuthToken(
                account,
                "oauth2:" + scope,
                null,
                fragment.requireActivity(),
                new AccountManagerCallback<Bundle>() {

                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Bundle result = future.getResult();
                            String token = result.getString(AccountManager.KEY_AUTHTOKEN);
                            log.debug(result.toString());
                        } catch (Exception e) {
                            log.error("failed", e);
                        }
                    }
                },
                new Handler(new Handler.Callback() {

                    @Override
                    public boolean handleMessage(Message msg) {
                        log.error(msg.toString());
                        return false;
                    }
                }));

    }

    @SuppressWarnings("deprecation")
    public void selectAccount() {
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            intent = AccountManager.newChooseAccountIntent(credential.getSelectedAccount(),
                    null, new String[]{"com.google"}, null, null, null, null);
        } else {
            intent = AccountManager.newChooseAccountIntent(credential.getSelectedAccount(),
                    null, new String[]{"com.google"}, false, null, null, null, null);
        }
        fragment.startActivityForResult(intent, REQUEST_ACCOUNT_PICKER);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
//                        credential.setSelectedAccountName(accountName);
//                        settings.edit()
//                                .putString(KEY_PREF_SELECTED_ACCOUNT, accountName)
//                                .apply();
                        requestPermission(accountName);
                        return true;
                    }
                }
                break;
        }
        return false;
    }

}

