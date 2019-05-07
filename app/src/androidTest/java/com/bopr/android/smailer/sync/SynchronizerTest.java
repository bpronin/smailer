package com.bopr.android.smailer.sync;

import android.accounts.Account;
import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GoogleAuthorizationHelper;

import org.junit.Test;

public class SynchronizerTest {

    @Test
    public void testExecute() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Account account = GoogleAuthorizationHelper.primaryAccount(context);
        Database database = new Database(context);
        new Synchronizer(context, account, database).execute();
    }
}