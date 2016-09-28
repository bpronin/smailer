package com.bopr.android.smailer.ui;

import android.app.Dialog;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.R;
import com.crashlytics.android.Crashlytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric.sdk.android.Fabric;

/**
 * An activity that presents a set of application settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainActivity extends AppActivity {

    private static Logger log = LoggerFactory.getLogger("MainActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        init();
    }

    @NonNull
    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }

    private void init() {
        log.debug("Application init");
        /* key generation may take some time. we don't want to interrupt user
         when he set password at first time so we initializing keystore here */
        if (!Cryptor.isKeystoreInitialized()) {
            new AsyncTask<Void, Void, Void>() {

                private Dialog dialog;

                @Override
                protected void onPreExecute() {
                    dialog = new Dialog(MainActivity.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                    dialog.setContentView(R.layout.dialog_full_screen_progress);
                    dialog.show();
                }

                @Override
                protected Void doInBackground(Void... params) {
                    Cryptor.initKeystore(MainActivity.this);
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    dialog.dismiss();
                    super.onPostExecute(result);
                }
            }.execute();
        }
    }

}
