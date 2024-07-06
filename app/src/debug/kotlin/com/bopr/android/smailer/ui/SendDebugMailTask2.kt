package com.bopr.android.smailer.ui

import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.MailMessage
import com.bopr.android.smailer.R
import com.bopr.android.smailer.util.JavaMail
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.showToast

internal class SendDebugMailTask2(activity: FragmentActivity, private val recipient: String) :
    LongAsyncTask<Void?, Void?, Exception?>(activity) {

    override fun executeInBackground(vararg params: Void?): Exception? {
        val message = MailMessage(
            from = "boris.i.pronin@ya.ru",
            subject = "test subject",
            body = "test message from " + deviceName(),
            recipients = recipient
        )

        try {
            JavaMail.send("boris.i.pronin@ya.ru", "brave2*Resine", "smtp.yandex.com", 465, message)
        } catch (x: Exception) {
            return x
        }
        return null
    }

    override fun afterExecute(result: Exception?) {
        super.afterExecute(result)
        if (result != null) {
            InfoDialog(message = result.toString()).show(activity)
        } else {
            activity.showToast(R.string.operation_complete)
        }
    }

}