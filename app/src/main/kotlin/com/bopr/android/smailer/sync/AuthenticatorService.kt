package com.bopr.android.smailer.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * A bound service that instantiates the authenticator when started.
 *
 *
 * Required by synchronization framework.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class AuthenticatorService : Service() {

    private lateinit var authenticator: Authenticator

    override fun onCreate() {
        authenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return authenticator.iBinder
    }
}