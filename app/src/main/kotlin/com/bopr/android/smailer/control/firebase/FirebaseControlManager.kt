package com.bopr.android.smailer.control.firebase

import android.content.Context
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.SettingsAware
import com.bopr.android.smailer.external.Firebase.Companion.firebase
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.SingletonHolder

class FirebaseControlManager private constructor(private val context: Context) :
    SettingsAware(context) {

    fun startService() {
        context.firebase.subscribe()
    }

    override fun onSettingsChanged(settings: Settings, key: String) {
        if (key == PREF_MAIL_SENDER_ACCOUNT) {
            if (context.accounts.isGoogleAccountExists(
                    context.settings.getString(PREF_MAIL_SENDER_ACCOUNT)
                )
            ) {
                context.firebase.apply {
                    unsubscribe()
                    subscribe()
                }
            }
        }
    }

    companion object {

        private val log = Logger("FirebaseControl")

        private val singletonHolder = SingletonHolder { FirebaseControlManager(it) }
        internal fun Context.startFirebaseMessaging() =
            singletonHolder.getInstance(this).startService()
    }
}