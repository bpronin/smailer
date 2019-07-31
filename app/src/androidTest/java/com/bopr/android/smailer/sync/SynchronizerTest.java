package com.bopr.android.smailer.sync;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.bopr.android.smailer.BaseTest;
import com.bopr.android.smailer.Settings;

import org.junit.Rule;
import org.junit.Test;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.READ_CONTACTS;
import static com.bopr.android.smailer.GoogleAuthorizationHelper.primaryAccount;
import static com.bopr.android.smailer.Settings.KEY_PREF_FILTER_PHONE_BLACKLIST;
import static com.bopr.android.smailer.Settings.KEY_PREF_SYNC_ITEMS;
import static com.bopr.android.smailer.Settings.KEY_SYNC_TIME;
import static com.bopr.android.smailer.Settings.VAL_PREF_SYNC_FILTER_LISTS;
import static com.bopr.android.smailer.util.Util.asSet;

public class SynchronizerTest extends BaseTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(GET_ACCOUNTS, READ_CONTACTS);

    @Test
    public void testUpdateRemote() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Settings settings = new Settings(context);
        Synchronizer synchronizer = new Synchronizer(context, primaryAccount(context));

        settings.edit()
                .putLong(KEY_SYNC_TIME, System.currentTimeMillis())
                .putStringSet(KEY_PREF_SYNC_ITEMS, asSet(VAL_PREF_SYNC_FILTER_LISTS))
                .putString(KEY_PREF_FILTER_PHONE_BLACKLIST, "A,B,C")
                .apply();

        synchronizer.synchronize();

        assertEquals(asSet("A", "B", "C"), settings.getFilter().getPhoneBlacklist());

        settings.edit()
                .putLong(KEY_SYNC_TIME, 0) /* earlier than previous */
                .putString(KEY_PREF_FILTER_PHONE_BLACKLIST, "A,B,C,D")
                .apply();

        synchronizer.synchronize();

        assertEquals(asSet("A", "B", "C"), settings.getFilter().getPhoneBlacklist());

        settings.edit()
                .putLong(KEY_SYNC_TIME, System.currentTimeMillis()) /* later than previous */
                .putString(KEY_PREF_FILTER_PHONE_BLACKLIST, "A,B")
                .apply();

        synchronizer.synchronize();

        assertEquals(asSet("A", "B"), settings.getFilter().getPhoneBlacklist());
    }

}