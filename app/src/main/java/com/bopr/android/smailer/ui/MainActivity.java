package com.bopr.android.smailer.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuItem;
import com.bopr.android.smailer.Cryptor;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.util.ui.AppCompatPreferenceActivity;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.bopr.android.smailer.Settings.loadDefaultPreferences;

/**
 * An activity that presents a set of application settings.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class MainActivity extends AppCompatPreferenceActivity {

    private static Logger log = LoggerFactory.getLogger("MainActivity");

    private Preference recipientsPreference;
    private Preference serverPreference;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.debug("Application init");
        super.onCreate(savedInstanceState);
//        setupActionBar();

        loadDefaultPreferences(this);

        Fabric.with(this, new Crashlytics());

        /* key generation may take some time. we don't want to interrupt user
         when he set password at first time so we initializing keystore here */
        if (!Cryptor.isKeystoreInitialized()) {
            new InitKeystoreTask(this).execute();
        }
    }

    @Override
    public void onDestroy() {
//        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        super.onDestroy();
    }

//    /**
//     * Set up the {@link android.app.ActionBar}, if the API is available.
//     */
//    private void setupActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_main_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || ServerFragment.class.getName().equals(fragmentName)
                || RecipientsFragment.class.getName().equals(fragmentName)
                || RulesFragment.class.getName().equals(fragmentName)
                || MoreFragment.class.getName().equals(fragmentName)
                || LogFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            new AboutDialogFragment().showDialog(this);
        }

        return super.onOptionsItemSelected(item);
    }

    private static class InitKeystoreTask extends LongAsyncTask<Void, Void, Void> {

        InitKeystoreTask(Activity activity) {
            super(activity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Cryptor.initKeystore(getContext());
            return null;
        }
    }
}
