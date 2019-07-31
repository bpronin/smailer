package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.content.ContentResolver.SYNC_EXTRAS_EXPEDITED;
import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static android.content.ContentResolver.addPeriodicSync;
import static android.content.ContentResolver.removePeriodicSync;
import static android.content.ContentResolver.requestSync;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.bopr.android.smailer.GoogleAuthorizationHelper.primaryAccount;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_PHONE_BLACKLIST;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_PHONE_WHITELIST;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_TEXT_BLACKLIST;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_TEXT_WHITELIST;
import static com.bopr.android.smailer.Settings.KEY_SYNC_TIME;
import static java.lang.System.currentTimeMillis;

public class SyncManager {

    private static final Logger log = LoggerFactory.getLogger("SyncManager");

    private final Database database;
    private final Settings settings;
    private final DatabaseListener databaseListener = new DatabaseListener();
    private final SettingsListener settingsListener = new SettingsListener();
    private final Account account;

    public SyncManager(Context context) {
        database = new Database(context);
        database.registerListener(databaseListener);

        settings = new Settings(context);
        settings.registerOnSharedPreferenceChangeListener(settingsListener);

        account = primaryAccount(context);
        addPeriodicSync(account, AppContentProvider.AUTHORITY, Bundle.EMPTY, 0);
    }

    public void dispose() {
        removePeriodicSync(account, AppContentProvider.AUTHORITY, Bundle.EMPTY);
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener);
        database.unregisterListener(databaseListener);
    }

    private void updateMetaData() {
        settings.edit().putLong(KEY_SYNC_TIME, currentTimeMillis()).apply();
        log.debug("Metadata updated");
    }

    public static void syncNow(Context context) {
        Account account = primaryAccount(context);
        Bundle bundle = new Bundle();
        bundle.putBoolean(SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(SYNC_EXTRAS_EXPEDITED, true);
        requestSync(account, AppContentProvider.AUTHORITY, bundle);
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
                case KEY_PREF_FILTER_PHONE_BLACKLIST:
                case KEY_PREF_FILTER_PHONE_WHITELIST:
                case KEY_PREF_FILTER_TEXT_BLACKLIST:
                case KEY_PREF_FILTER_TEXT_WHITELIST:
                    updateMetaData();
            }
        }
    }
}


