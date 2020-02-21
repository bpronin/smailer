package com.bopr.android.smailer.ui

import android.app.Activity
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.GoogleMail
import com.bopr.android.smailer.MailMessage
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.primaryAccount
import com.bopr.android.smailer.util.showInfoDialog
import com.bopr.android.smailer.util.showToast
import com.google.api.services.gmail.GmailScopes
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*

internal class SendLogTask(activity: Activity, private val properties: Properties)
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
            val sender = primaryAccount(activity).name
            val transport = GoogleMail(activity)

            transport.startSession(sender, GmailScopes.GMAIL_SEND)
            for (file in attachments) {
                val message = MailMessage().apply {
                    subject = "[SMailer] log: " + file.name
                    from = sender
                    body = "Device: " + deviceName() + "<br>File: " + file.name
                    attachment = setOf(file)
                    recipients = properties.getProperty("developer_email")
                }
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
            showInfoDialog(activity, message = result.toString())
        } else {
            showToast(activity, "Done")
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