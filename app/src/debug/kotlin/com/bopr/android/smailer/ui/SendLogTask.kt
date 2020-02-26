package com.bopr.android.smailer.ui

import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.GoogleMail
import com.bopr.android.smailer.MailMessage
import com.bopr.android.smailer.R
import com.bopr.android.smailer.ui.GoogleAuthorizationHelper.Companion.primaryAccount
import com.bopr.android.smailer.util.AndroidUtil.deviceName
import com.bopr.android.smailer.util.UiUtil.showToast
import com.google.api.services.gmail.GmailScopes
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*

internal class SendLogTask(activity: FragmentActivity, private val properties: Properties)
    : LongAsyncTask<Void?, Void?, Exception?>(activity) {

    private val log = LoggerFactory.getLogger("SendLogTask")

    override fun doInBackground(vararg params: Void?): Exception? {
        val attachments: MutableList<File> = mutableListOf()
        attachments.add(activity.getDatabasePath(Database.DATABASE_NAME))
        attachments.add(readLogcatLog())

        File(activity.filesDir, "log").listFiles()?.let {
            attachments.addAll(it)
        }

        try {
            val account = primaryAccount(activity)!!
            val transport = GoogleMail(activity)

            transport.login(account, GmailScopes.GMAIL_SEND)
            transport.startSession()
            for (file in attachments) {
                val message = MailMessage(
                        subject = "[SMailer] log: " + file.name,
                        from = account.name,
                        body = "Device: " + deviceName() + "<br>File: " + file.name,
                        attachment = setOf(file),
                        recipients = properties.getProperty("developer_email")
                )
                transport.send(message)
            }
        } catch (x: Exception) {
            log.error("Send mail failed", x)
            return x
        }
        return null
    }

    override fun onPostExecute(result: Exception?) {
        super.onPostExecute(result)
        if (result != null) {
            InfoDialog(message = result.toString()).show(activity)
        } else {
            showToast(activity, R.string.operation_complete)
        }
    }

    private fun readLogcatLog(): File {
        val file = File(activity.filesDir, "logcat.log")
        try {
            val process = Runtime.getRuntime().exec("logcat -d")
            val src = BufferedReader(InputStreamReader(process.inputStream))
            val dst = PrintWriter(FileOutputStream(file))
            var line: String?
            while (src.readLine().also { line = it } != null) {
                dst.println(line)
            }
            src.close()
            dst.close()
        } catch (x: IOException) {
            log.error("Cannot get logcat ", x)
        }
        return file
    }
}