package com.bopr.android.smailer.ui

import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.GoogleMail
import com.bopr.android.smailer.MailMessage
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.primaryAccount
import com.bopr.android.smailer.util.showToast
import com.google.api.services.gmail.GmailScopes

internal class SendDebugMailTask(activity: FragmentActivity, private val recipient: String)
    : LongAsyncTask<Void?, Void?, Exception?>(activity) {

    override fun doInBackground(vararg params: Void?): Exception? {
        val account = primaryAccount(activity)!!

        val message = MailMessage(
                from = account.name,
                subject = "test subject",
                body = "test message from " + deviceName(),
                recipients = recipient
        )

        val transport = GoogleMail(activity)
        try {
            transport.login(account, GmailScopes.GMAIL_SEND)
            transport.send(message)
        } catch (x: Exception) {
            return x
        }
        return null
    }

    override fun onPostExecute(result: Exception?) {
        super.onPostExecute(result)
        if (result != null) {
            InfoDialog(message = result.toString()).show(activity)
        } else {
            activity.showToast(R.string.operation_complete)
        }
    }

}