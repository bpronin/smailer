package com.bopr.android.smailer.ui

import android.app.Activity
import com.bopr.android.smailer.GoogleMail
import com.bopr.android.smailer.MailMessage
import com.bopr.android.smailer.ui.GoogleAuthorizationHelper.Companion.primaryAccount
import com.bopr.android.smailer.util.AndroidUtil.deviceName
import com.bopr.android.smailer.util.Dialogs.showInfoDialog
import com.bopr.android.smailer.util.UiUtil.showToast
import com.google.api.services.gmail.GmailScopes
import java.util.*

internal class SendDebugMailTask(activity: Activity, private val properties: Properties)
    : LongAsyncTask<Void?, Void?, Exception?>(activity) {

    override fun doInBackground(vararg params: Void?): Exception? {
        val account = primaryAccount(activity)!!

        val message = MailMessage(
                from = account.name,
                subject = "test subject",
                body = "test message from " + deviceName(),
                recipients = properties.getProperty("default_recipient")
        )

        val transport = GoogleMail(activity)
        try {
            transport.login(account, GmailScopes.GMAIL_SEND)
            transport.startSession()
            transport.send(message)
        } catch (x: Exception) {
            return x
        }
        return null
    }

    override fun onPostExecute(result: Exception?) {
        super.onPostExecute(result)
        if (result != null) {
            showInfoDialog(activity, result.toString())
        } else {
            showToast(activity, "Done")
        }
    }

}