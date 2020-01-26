package com.bopr.android.smailer.sync;

import android.accounts.Account;
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
import static com.bopr.android.smailer.Settings.PREF_FILTER_PHONE_BLACKLIST;
import static com.bopr.android.smailer.Settings.PREF_SYNC_TIME;
import static com.bopr.android.smailer.util.Util.asSet;
import static org.junit.Assert.assertEquals;

public class SyncAdapterTest extends BaseTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(GET_ACCOUNTS, READ_CONTACTS);

    @Test
    public void testUpdateRemote() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Settings settings = new Settings(context);
        Account account = primaryAccount(context);
        SyncAdapter adapter = new SyncAdapter(context, true);

        settings.edit()
                .putLong(PREF_SYNC_TIME, System.currentTimeMillis())
                .putString(PREF_FILTER_PHONE_BLACKLIST, "A,B,C")
                .apply();

        adapter.onPerformSync(account, null, null, null, null);

        assertEquals(asSet("A", "B", "C"), settings.getFilter().getPhoneBlacklist());

        settings.edit()
                .putLong(PREF_SYNC_TIME, 0) /* earlier than previous */
                .putString(PREF_FILTER_PHONE_BLACKLIST, "A,B,C,D")
                .apply();

        adapter.onPerformSync(account, null, null, null, null);

        assertEquals(asSet("A", "B", "C"), settings.getFilter().getPhoneBlacklist());

        settings.edit()
                .putLong(PREF_SYNC_TIME, System.currentTimeMillis()) /* later than previous */
                .putString(PREF_FILTER_PHONE_BLACKLIST, "A,B")
                .apply();

        adapter.onPerformSync(account, null, null, null, null);

        assertEquals(asSet("A", "B"), settings.getFilter().getPhoneBlacklist());
    }

}