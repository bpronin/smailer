package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.bopr.android.smailer.GoogleAuthorizationHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.content.ContentResolver.SYNC_EXTRAS_EXPEDITED;
import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static android.content.ContentResolver.requestSync;

/**
 * Handle the transfer of data between a server and an app, using the Android sync adapter framework.
 * <p>
 * Required by synchronization framework.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
/* To debug it (to put breakpoints) remove android:process=":sync" from AndroidManifest */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final Logger log = LoggerFactory.getLogger("SyncAdapter");

    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        try {
            new Synchronizer(getContext(), account).synchronize();
        } catch (Exception x) {
            log.error("Synchronization failed ", x);
        }
    }

    private static Account getSyncAccount(Context context) {
        return GoogleAuthorizationHelper.selectedAccount(context);
    }

    public static void syncNow(Context context) {
        Account account = getSyncAccount(context);
        if (account != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(SYNC_EXTRAS_MANUAL, true);
            bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true);
            requestSync(account, AppContentProvider.AUTHORITY, bundle);
        } else {
            log.warn("No sync account specified");
        }
    }

}


