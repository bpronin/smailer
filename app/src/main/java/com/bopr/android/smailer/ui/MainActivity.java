package com.bopr.android.smailer.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.bopr.android.smailer.OutgoingSmsService;
import com.bopr.android.smailer.ResendService;
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

    public MainActivity() {
        setClosable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log.debug("Application init");
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        OutgoingSmsService.toggleService(this);
        ResendService.toggleService(this);
    }

    @NonNull
    @Override
    protected Fragment createFragment() {
//        return AndroidUtil.isXLargeTablet(this) ? new MainFragmentDual() : new MainFragment();
        return new MainFragment();
    }

}
