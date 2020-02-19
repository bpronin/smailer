package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.Settings;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.content.ContentResolver.SYNC_EXTRAS_EXPEDITED;
import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static android.content.ContentResolver.addPeriodicSync;
import static android.content.ContentResolver.removePeriodicSync;
import static android.content.ContentResolver.requestSync;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.bopr.android.smailer.Database.registerDatabaseListener;
import static com.bopr.android.smailer.Settings.PREF_FILTER_PHONE_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_PHONE_WHITELIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_TEXT_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_FILTER_TEXT_WHITELIST;
import static com.bopr.android.smailer.Settings.PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.PREF_SYNC_TIME;
import static com.bopr.android.smailer.Settings.settings;
import static com.bopr.android.smailer.sync.AppContentProvider.AUTHORITY;
import static java.lang.System.currentTimeMillis;

public class SyncManager {

    private static final Logger log = LoggerFactory.getLogger("SyncManager");

    private final Database database;
    private final Context context;
    private final Settings settings;
    private final DatabaseListener databaseListener = new DatabaseListener();
    private final SettingsListener settingsListener = new SettingsListener();
    private Account account;

    private SyncManager(Context context) {
        this.context = context;
        database = new Database(context);
        registerDatabaseListener(context, databaseListener);

        settings = new Settings(context);
        settings.registerOnSharedPreferenceChangeListener(settingsListener);
    }

//    public void dispose() {
//        stop();
//        settings.unregisterOnSharedPreferenceChangeListener(settingsListener);
//        database.unregisterListener(databaseListener);
//    }

    private void start() {
        account = syncAccount(context);
        if (account != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(SYNC_EXTRAS_MANUAL, true);
            bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true);
            requestSync(account, AUTHORITY, bundle);

            addPeriodicSync(account, AUTHORITY, Bundle.EMPTY, 0);

            log.debug("Running");
        } else {
            log.debug("No selected account");
        }
    }

    private void stop() {
        if (account != null) {
            removePeriodicSync(account, AUTHORITY, Bundle.EMPTY);

            log.debug("Stopped");
        }
    }

    private void updateMetaData() {
        settings.edit().putLong(PREF_SYNC_TIME, currentTimeMillis()).apply();

        log.debug("Metadata updated");
    }

    private static Account syncAccount(Context context) {
        String name = settings(context).getString(PREF_SENDER_ACCOUNT);
        return new GoogleAccountManager(context).getAccountByName(name);
    }

    public static void enable(Context context) {
        new SyncManager(context).start();
    }

    public static void syncNow(Context context) {
        Account account = syncAccount(context);

        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true);
        requestSync(account, AUTHORITY, bundle);
    }

    private class DatabaseListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateMetaData();
        }

    }

    private class SettingsListener implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PREF_FILTER_PHONE_BLACKLIST:
                case PREF_FILTER_PHONE_WHITELIST:
                case PREF_FILTER_TEXT_BLACKLIST:
                case PREF_FILTER_TEXT_WHITELIST:
                    updateMetaData();
                    break;
                case PREF_SENDER_ACCOUNT:
                    stop();
                    start();
                    break;
            }
        }
    }
}


