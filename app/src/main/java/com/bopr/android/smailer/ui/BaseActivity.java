package com.bopr.android.smailer.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import com.bopr.android.smailer.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Base Activity with default behaviour.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG_FRAGMENT = "activity_fragment";

    private Fragment fragment;
    private boolean closable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragment = fragmentManager.findFragmentByTag(TAG_FRAGMENT);
        if (fragment == null) {
            fragment = createFragment();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.content, fragment, TAG_FRAGMENT)
                    .commit();
        }

        setupActionBar();
    }

    @SuppressWarnings("SameParameterValue")
    void setClosable(boolean closable) {
        this.closable = closable;
    }

    @NonNull
    protected abstract Fragment createFragment();

    private void setupActionBar() {
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

}
