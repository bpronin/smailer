package com.bopr.android.smailer.ui

import android.app.Activity
import com.bopr.android.smailer.GoogleMail
import com.bopr.android.smailer.MailMessage
import com.bopr.android.smailer.util.AndroidUtil.deviceName
import com.bopr.android.smailer.util.AndroidUtil.primaryAccount
import com.bopr.android.smailer.util.UiUtil.showInfoDialog
import com.bopr.android.smailer.util.UiUtil.showToast
import com.bopr.android.smailer.util.Util.requireNonNull
import com.google.api.services.gmail.GmailScopes
import java.util.*

internal class SendDebugMailTask(activity: Activity, private val properties: Properties)
    : LongAsyncTask<Void?, Void?, Exception?>(activity) {

    override fun doInBackground(vararg params: Void?): Exception? {
        val transport = GoogleMail(activity)
        val sender = primaryAccount(activity).name
        val message = MailMessage().apply {
            from = sender
            subject = "test subject"
            body = "test message from " + deviceName()
            recipients = requireNonNull(properties.getProperty("default_recipient"))
        }

        try {
            transport.startSession(sender, GmailScopes.GMAIL_SEND)
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