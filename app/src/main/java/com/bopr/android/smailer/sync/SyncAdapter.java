package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.bopr.android.smailer.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle the transfer of data between a server and an app, using the Android sync adapter framework.
 * <p>
 * Required by synchronization framework.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final Logger log = LoggerFactory.getLogger("SyncAdapter");

    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        log.warn("Synchronizing");

        try(Database database = new Database(getContext())) {
            new Synchronizer(getContext(), account, database).sync();
        } catch (Exception x) {
            log.warn("Synchronization failed ", x);
        }
    }
}


