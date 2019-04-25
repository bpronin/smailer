package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.bopr.android.smailer.GoogleAuthorizationHelper;

import static com.bopr.android.smailer.util.Util.requireNonNull;

public class SyncUtil {

    private static final String TAG = "SyncUtil";

    public static final String AUTHORITY = "com.bopr.android.smailer.provider";
//    private static final String ACCOUNT_TYPE = "com.bopr.android.smailer";
//    private static final String ACCOUNT = "sync";

    /**
     * Get a dummy account for the sync adapter.
     *
     * @param context The application context
     */
    private static Account getSyncAccount(Context context) {
        return requireNonNull(GoogleAuthorizationHelper.selectedAccount(context));
//        String accountType = getAccountType(context);
//        AccountManager manager = AccountManager.get(context);
//        Account[] accounts = manager.getAccountsByType(accountType);
//        if (accounts.length != 0) {
//            return accounts[0];
//        } else {
//            throw new IllegalStateException("Sync account not found");
//            Account account = new Account(ACCOUNT, accountType);
//            if (!manager.addAccountExplicitly(account, null, null)) {
//                throw new IllegalStateException("Cannot create sync account");
//            }
//            return account;
//        }
    }

    public static void syncNow(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(getSyncAccount(context), AUTHORITY, bundle);
    }

}
