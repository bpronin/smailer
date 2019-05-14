package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GoogleAuthorizationHelper;
import com.bopr.android.smailer.Settings;

import org.junit.Test;

import static com.bopr.android.smailer.Settings.KEY_PREF_SYNC_ITEMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_SYNC_FILTER_LISTS;
import static com.bopr.android.smailer.util.Util.asSet;

public class SynchronizerTest {

    @Test
    public void testExecute() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Settings settings = new Settings(context);
        settings.edit().putStringSetOptional(KEY_PREF_SYNC_ITEMS,
                asSet(VAL_PREF_SYNC_FILTER_LISTS)).apply();
        Account account = GoogleAuthorizationHelper.primaryAccount(context);
        Database database = new Database(context);
        Synchronizer synchronizer = new Synchronizer(context, account, database, settings);
        synchronizer.synchronize();
    }
}