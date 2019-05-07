package com.bopr.android.smailer.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * A bound service that instantiates the authenticator when started.
 * <p>
 * Required by synchronization framework.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class AuthenticatorService extends Service {

    private Authenticator authenticator;

    @Override
    public void onCreate() {
        authenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}