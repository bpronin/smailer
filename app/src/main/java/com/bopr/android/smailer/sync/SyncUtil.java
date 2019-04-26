package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;

import com.bopr.android.smailer.GoogleAuthorizationHelper;

import static android.content.ContentResolver.SYNC_EXTRAS_EXPEDITED;
import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static android.content.ContentResolver.requestSync;
import static com.bopr.android.smailer.util.Util.requireNonNull;

public class SyncUtil {

    private static Account getSyncAccount(Context context) {
        return GoogleAuthorizationHelper.selectedAccount(context);
    }

    public static void syncNow(Context context) {
        Account account = requireNonNull(getSyncAccount(context));
        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true);
        requestSync(account, AppContentProvider.AUTHORITY, bundle);
    }

}
