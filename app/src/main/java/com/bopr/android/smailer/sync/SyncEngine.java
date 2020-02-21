package com.bopr.android.smailer.sync;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.bopr.android.smailer.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.content.ContentResolver.SYNC_EXTRAS_EXPEDITED;
import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static android.content.ContentResolver.addPeriodicSync;
import static android.content.ContentResolver.requestSync;
import static com.bopr.android.smailer.Database.registerDatabaseListener;
import static com.bopr.android.smailer.Settings.PREF_FILTER_PHONE_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_PHONE_WHITELIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_TEXT_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_TEXT_WHITELIST;
import static com.bopr.android.smailer.Settings.PREF_SYNC_TIME;
import static com.bopr.android.smailer.sync.AppContentProvider.AUTHORITY;
import static com.bopr.android.smailer.util.AndroidUtil.primaryAccount;
import static java.lang.System.currentTimeMillis;

public class SyncEngine {

    private static final Logger log = LoggerFactory.getLogger("SyncEngine");

    private SyncEngine() {
    }

    public static void startSyncEngine(@NonNull Context context) {
        registerDatabaseListener(context, () -> updateMetadata(context));
        addPeriodicSync(primaryAccount(context), AUTHORITY, Bundle.EMPTY, 0);

        log.debug("Running");
    }

    public static void onSyncSettingsChanged(@NonNull Context context, @NonNull String key) {
        switch (key) {
            case PREF_FILTER_PHONE_BLACKLIST:
            case PREF_FILTER_PHONE_WHITELIST:
            case PREF_FILTER_TEXT_BLACKLIST:
            case PREF_FILTER_TEXT_WHITELIST:
                updateMetadata(context);
                break;
        }
    }

    public static void syncNow(@NonNull Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true);

        requestSync(primaryAccount(context), AUTHORITY, bundle);

        log.debug("Sync now");
    }

    private static void updateMetadata(Context context) {
        new Settings(context).edit().putLong(PREF_SYNC_TIME, currentTimeMillis()).apply();

        log.debug("Metadata updated");
    }
}


