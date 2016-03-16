package com.bopr.android.smailer.settings;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.bopr.android.smailer.util.AppCompatActivity;

public abstract class DefaultPreferenceActivity extends AppCompatActivity {

    private Fragment fragment;
    private boolean closable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragment = createFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();

        setupActionBar();
    }

    public void setClosable(boolean closable) {
        this.closable = closable;
    }

    @NonNull
    protected abstract Fragment createFragment();

    protected void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(closable);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (closable && item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

/*
    Sliding on finish() looks ugly.
    -------------------------------
    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }



    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
    }
*/

}
