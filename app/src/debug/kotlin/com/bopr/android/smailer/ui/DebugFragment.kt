package com.bopr.android.smailer.ui

import android.Manifest.permission.READ_CONTACTS
import android.accounts.Account
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE
import android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE
import android.telephony.SmsManager.RESULT_ERROR_NULL_PDU
import android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF
import android.text.InputType
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_GOOGLE_ACCOUNT
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_MAIL_RECIPIENTS
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_SERVICE_ACCOUNT
import com.bopr.android.smailer.NotificationsHelper.Companion.notifications
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_MESSAGE_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_MAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_MESSAGE_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_PHONE_PROCESS_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_DEFAULT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CREATION_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_CALLS
import com.bopr.android.smailer.Settings.Companion.settings
import com.bopr.android.smailer.Settings.Companion.sharedPreferencesName
import com.bopr.android.smailer.control.mail.MailControlProcessor
import com.bopr.android.smailer.data.Database.Companion.database
import com.bopr.android.smailer.external.Firebase.Companion.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.external.Firebase.Companion.firebase
import com.bopr.android.smailer.external.GoogleDrive
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.Event.Companion.FLAG_BYPASS_NO_CONSUMERS
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_IGNORED
import com.bopr.android.smailer.messenger.ProcessState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.messenger.mail.GoogleMailSession
import com.bopr.android.smailer.messenger.mail.MailMessage
import com.bopr.android.smailer.messenger.telegram.TelegramSession
import com.bopr.android.smailer.provider.telephony.PhoneCallData
import com.bopr.android.smailer.provider.telephony.PhoneCallEventProcessor
import com.bopr.android.smailer.provider.telephony.PhoneCallEventProcessor.Companion.processPhoneCall
import com.bopr.android.smailer.sync.Synchronizer
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_FORCE_DOWNLOAD
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_FORCE_UPLOAD
import com.bopr.android.smailer.ui.InfoDialog.Companion.showInfoDialog
import com.bopr.android.smailer.util.BackgroundActivityHelper
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.GeoLocation.Companion.getGeoLocation
import com.bopr.android.smailer.util.Logger
import com.bopr.android.smailer.util.PreferenceProgress
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.escapeRegex
import com.bopr.android.smailer.util.getContactName
import com.bopr.android.smailer.util.readLogcatLog
import com.bopr.android.smailer.util.runBackgroundTask
import com.bopr.android.smailer.util.sendSmsMessage
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.showToast
import com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM
import java.io.File
import java.lang.System.currentTimeMillis

/**
 * For debug purposes.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class DebugFragment : PreferenceFragmentCompat() {

    private lateinit var authorizationHelper: GoogleAuthorizationHelper
    private lateinit var smsSendStatusReceiver: BroadcastReceiver
    private lateinit var smsDeliveryStatusReceiver: BroadcastReceiver
    private lateinit var backgroundActivityHelper: BackgroundActivityHelper
    private val developerEmail by lazy { getString(R.string.developer_email) }

    //    private val requestPermissionLauncher =
//        registerForActivityResult(RequestPermission()) { result: Boolean ->
//            onPermissionRequestResult(result)
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = sharedPreferencesName

        authorizationHelper = GoogleAuthorizationHelper(
            requireActivity(), PREF_MAIL_SENDER_ACCOUNT, MAIL_GOOGLE_COM, DRIVE_APPDATA
        )

        smsSendStatusReceiver = SentStatusReceiver().also {
            registerReceiver(it, IntentFilter("SMS_SENT"))
        }

        smsDeliveryStatusReceiver = DeliveryStatusReceiver().also {
            registerReceiver(it, IntentFilter("SMS_DELIVERED"))
        }

        backgroundActivityHelper = BackgroundActivityHelper(requireActivity()) {
            showComplete()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        /* do not use fragment's context. see: https://developer.android.com/guide/topics/ui/settings/programmatic-hierarchy*/
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)
        addCategory(
            screen, "Event processing",
            addPreference("Process single event") {
                onProcessSingle()
            },
            addPreference("Process pending events") {
                onProcessPending(it)
            },
            addPreference("Send SMS") {
                onSendSms()
            }
        )
        addCategory(
            screen, "Telegram",
            addPreference("Send debug message") {
                onSendTelegramMessage()
            }
        )
        addCategory(
            screen, "Email",
            addPreference("Send debug mail") {
                onSendDebugMail(it)
            }
        )
        addCategory(
            screen, "Settings",
            addPreference("Populate settings") {
                onSetDebugPreferences()
            },
            addPreference("Clear settings") {
                onClearPreferences()
            },
            addPreference("Load default settings") {
                onResetPreferences()
            },
            addPreference("Put invalid account") {
                onPutInvalidAccount()
            },
            addPreference("Check ignoring battery optimisation") {
                backgroundActivityHelper.check()
            }
        )
        addCategory(
            screen, "Google drive",
            addPreference("Download from drive") {
                onGoogleDriveDownload(it)
            },
            addPreference("Upload to drive") {
                onGoogleDriveUpload(it)
            },
            addPreference("Clear remote data") {
                onGoogleDriveClear(it)
            },
            addPreference("Sync data") {
                onGoogleDriveSync(it)
            }
        )
        addCategory(
            screen, "Firebase",
            addPreference("Send message") {
                requireContext().firebase.send(FCM_REQUEST_DATA_SYNC)
            },
            addPreference("Subscribe") {
                requireContext().firebase.subscribe()
            },
            addPreference("Unsubscribe") {
                requireContext().firebase.unsubscribe()
            },
            addPreference("Get current token") {
                requireContext().firebase.requestToken { token ->
                    showInfoDialog("Firebase token", token)
                }
            },
            addPreference("Get server info") {
                requireContext().firebase.requestInfo { info ->
                    showInfoDialog("Info", info)
                }
            }
        )
        addCategory(
            screen, "Database",
            addPreference("Add an item to calls log") {
                onAddHistoryItem()
            },
            addPreference("Add 10 items to calls log") {
                onPopulateHistory()
            },
            addPreference("Mark all as unread") {
                database.commit { batch { events.markAllAsRead(false) } }
                showComplete()
            },
            addPreference("Mark all as read") {
                database.commit { batch { events.markAllAsRead(true) } }
                showComplete()
            },
            addPreference("Clear calls log") {
                database.commit { batch { events.clear() } }
                showComplete()
            },
            addPreference("Destroy database") {
                database.destroy()
                showComplete()
            }
        )
        addCategory(
            screen, "Permissions",
            addPreference("Request gmail api permission") {
                onRequestGooglePermission()
            }
        )
        addCategory(
            screen, "Logging",
            addPreference("Send logs to developer") {
                onSendLog(it)
            },
            addPreference("Clear logs") {
                onClearLogs()
            }
        )
        addCategory(
            screen, "Notifications",
            addPreference("Cancel errors") {
                notifications.cancelError(NTF_GOOGLE_ACCOUNT)
                notifications.cancelError(NTF_SERVICE_ACCOUNT)
                notifications.cancelError(NTF_MAIL_RECIPIENTS)
            }
        )
        addCategory(
            screen, "Email remote control",
            addPreference("Process service mail") {
                onProcessServiceMail(it)
            }
        )
        addCategory(
            screen, "Other",
            addPreference("Get geolocation") {
                onGetGeoLocation(it)
            },
            addPreference("Get contact") {
                onGetContact()
            },
            addPreference("Show accounts") {
                onShowAccounts()
            },
            addPreference("Show concurrent applications") {
                onShowConcurrentApps()
            },
            addPreference("Crash!") {
                throw RuntimeException("Test crash")
            }
        )
        preferenceScreen = screen
    }

    private fun onPutInvalidAccount() {
        settings.update {
            putString(PREF_MAIL_SENDER_ACCOUNT, "unknown@gmail.com")
        }
    }

    private fun onSendLog(preference: Preference) {
        val progress = PreferenceProgress(preference).apply { start() }
        val context = requireContext()

        val attachments: MutableList<File> = mutableListOf()
        attachments.add(context.getDatabasePath(database.name))
        attachments.add(context.readLogcatLog())

        File(context.filesDir, "log").listFiles()?.let {
            attachments.addAll(it)
        }

        val account = accounts.requirePrimaryGoogleAccount()
        val session = GoogleMailSession(context, account, GMAIL_SEND)
        for (file in attachments) {
            val message = MailMessage(
                subject = "[SMailer] log: " + file.name,
                from = account.name,
                body = "Device: " + DEVICE_NAME + "<br>File: " + file.name,
                attachment = setOf(file),
                recipients = developerEmail
            )

            session.send(
                message,
                onSuccess = {
                    progress.stop()
                    showComplete()
                },
                onError = { error ->
                    progress.stop()
                    showError("Log", error)
                }
            )
        }
    }

    private fun onSendDebugMail(preference: Preference) {
        val progress = PreferenceProgress(preference).apply { start() }
        val account = accounts.requirePrimaryGoogleAccount()

        val message = MailMessage(
            from = account.name,
            subject = "test subject",
            body = "test message from $DEVICE_NAME",
            recipients = developerEmail
        )

        GoogleMailSession(requireContext(), account, GMAIL_SEND).send(
            message,
            onSuccess = {
                progress.stop()
                showComplete()
            },
            onError = {
                progress.stop()
                showError("Mail", it)
            }
        )
    }

    private fun onGetGeoLocation(preference: Preference) {
        preference.runBackgroundTask(
            onPerform = {
                requireContext().getGeoLocation()
            },
            onSuccess = {
                showInfoDialog("Geolocation", it?.format() ?: "No geolocation received")
            },
            onError = { error ->
                showError("Geolocation", error)
            }
        )
    }

    override fun onDestroy() {
        unregisterReceiver(smsSendStatusReceiver)
        unregisterReceiver(smsDeliveryStatusReceiver)
        database.close()
        super.onDestroy()
    }

//    private fun onPermissionRequestResult(granted: Boolean) {
//        if (granted) {
//            showInfoDialog("Permission", "Granted")
//        } else {
//            showInfoDialog("Permission", "Denied")
//        }
//    }

    private fun addPreference(title: String, onClick: (Preference) -> Unit): Preference {
        return Preference(requireContext()).apply {
            setIcon(R.drawable.ic_bullet)
            this.title = title
            setOnClickListener(onClick)
        }
    }

    private fun addCategory(
        screen: PreferenceScreen,
        title: String,
        vararg preferences: Preference
    ) {
        PreferenceCategory(requireContext()).apply {
            screen.addPreference(this)
            this.title = title
            preferences.forEach(::addPreference)
        }
    }

    private fun onSetDebugPreferences() {
        settings.update {
            putString(PREF_MAIL_SENDER_ACCOUNT, developerEmail)
            putString(PREF_REMOTE_CONTROL_ACCOUNT, developerEmail)
            putStringList(
                PREF_MAIL_MESSENGER_RECIPIENTS,
                setOf(developerEmail, "nowhere@mail.com")
            )
            putStringSet(
                PREF_PHONE_PROCESS_TRIGGERS, mutableSetOf(
                    VAL_PREF_TRIGGER_IN_SMS,
                    VAL_PREF_TRIGGER_IN_CALLS,
                    VAL_PREF_TRIGGER_MISSED_CALLS,
                    VAL_PREF_TRIGGER_OUT_CALLS,
                )
            )
            putStringSet(
                PREF_MAIL_MESSAGE_CONTENT, mutableSetOf(
                    VAL_PREF_MESSAGE_CONTENT_CALLER,
                    VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                    VAL_PREF_MESSAGE_CONTENT_LOCATION,
                    VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME,
                    VAL_PREF_MESSAGE_CONTENT_HEADER,
                    VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS,
                    VAL_PREF_MESSAGE_CONTENT_CREATION_TIME
                )
            )
            putString(PREF_MESSAGE_LOCALE, VAL_PREF_DEFAULT)
            putBoolean(PREF_NOTIFY_SEND_SUCCESS, true)
        }

        database.phoneBlacklist.replaceAll(setOf("+123456789", "+9876543*"))
        database.textBlacklist.replaceAll(setOf("Bad text", escapeRegex("Expression")))

        showComplete()
    }

    private fun onGetContact() {
        if (checkPermission(READ_CONTACTS)) {
            InputDialog(
                title = "Phone number",
                inputType = InputType.TYPE_CLASS_PHONE,
                positiveAction = {
                    val contact = requireContext().getContactName(it)
                    val text = if (contact != null) "$it: $contact" else "Contact not found"
                    showInfoDialog("Contact", text)
                }
            ).show(this)
        } else {
            showInfoDialog("Contact", "Missing required permission")
        }
    }

    private fun onClearPreferences() {
        settings.update { clear() }
        showComplete()
    }

    private fun onResetPreferences() {
        settings.loadDefaults()
        showComplete()
    }

    private fun onProcessServiceMail(preference: Preference) {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_ENABLED)) {
            val progress = PreferenceProgress(preference).apply { start() }
            MailControlProcessor(requireContext()).checkMailbox(
                onSuccess = {
                    progress.stop()
                    showComplete()
                },
                onError = {
                    progress.stop()
                    showError("Remote control", it)
                })
        } else {
            showInfoDialog("Remote control", "Feature is disabled")
        }
    }

    private fun onRequestGooglePermission() {
        authorizationHelper.startAccountPicker()
    }

    private fun onProcessSingle() {
        val start = currentTimeMillis()
        requireContext().processPhoneCall(
            PhoneCallData(
                startTime = start,
                phone = "+1(234) 567-89-01",
                isIncoming = true,
                endTime = start + 10000,
                text = "Debug SMS message text"
            )
        )
        showComplete()
    }

    private fun onProcessPending(preference: Preference) {
        preference.runBackgroundTask(
            onPerform = {
                PhoneCallEventProcessor(requireContext()).process()
            },
            onSuccess = { it ->
                showInfoDialog("Event processing", "$it events processed")
            },
            onError = { error ->
                showError("Event processing", error)
            }
        )
    }

    private fun onClearLogs() {
        val dir = File(requireContext().filesDir, "log")
        val logs = dir.listFiles()!!
        for (file in logs) {
            if (!file.delete()) {
                log.warn("Cannot delete file")
            }
        }
        showInfoDialog("Log", "Removed ${logs.size} log files")
    }

    private fun onAddHistoryItem() {
        database.commit {
            events.add(
                Event(
                    payload = PhoneCallData(
                        startTime = currentTimeMillis(),
                        phone = "+79052345670",
                        isIncoming = true,
                        text = "Debug message"
                    )
                )
            )
        }
        showComplete()
    }

    private fun onPopulateHistory() {
        var time = currentTimeMillis()
        database.commit {
            batch {
                events.apply {
                    add(
                        Event(
                            payload = PhoneCallData(
                                startTime = time,
                                phone = "+79052345671",
                                isIncoming = true,
                                text = "Debug message"
                            )
                        )
                    )
                    add(
                        Event(
                            processState = STATE_PROCESSED,
                            payload = PhoneCallData(
                                startTime = 1000.let { time += it; time },
                                phone = "+79052345672",
                                text = "Debug message"
                            )
                        )
                    )
                    add(
                        Event(
                            processState = STATE_IGNORED,
                            payload = PhoneCallData(
                                startTime = 1000.let { time += it; time },
                                phone = "+79052345673",
                                isIncoming = true,
                                endTime = time + 10000
                            )
                        )
                    )
                    add(
                        Event(
                            isRead = true,
                            payload = PhoneCallData(
                                startTime = 1000.let { time += it; time },
                                phone = "+79052345674",
                                endTime = time + 10000
                            )
                        )
                    )
                    add(
                        Event(
                            bypassFlags = FLAG_BYPASS_NO_CONSUMERS,
                            payload = PhoneCallData(
                                startTime = 1000.let { time += it; time },
                                phone = "+79052345675",
                                isIncoming = true,
                                endTime = time + 10000,
                                isMissed = true
                            )
                        )
                    )
                    add(
                        Event(
                            payload = PhoneCallData(
                                startTime = 1000.let { time += it; time },
                                phone = "+79052345671",
                                isIncoming = true,
                                text = "Debug message"
                            )
                        )
                    )
                    add(
                        Event(
                            payload = PhoneCallData(
                                startTime = 1000.let { time += it; time },
                                phone = "+79052345672",
                                text = "Debug message"
                            )
                        )
                    )
                    add(
                        Event(
                            payload = PhoneCallData(
                                startTime = 1000.let { time += it; time },
                                phone = "+79052345673",
                                isIncoming = true,
                                endTime = time + 10000
                            )
                        )
                    )
                    add(
                        Event(
                            payload = PhoneCallData(
                                startTime = 1000.let { time += it; time },
                                phone = "+79052345674",
                                endTime = time + 10000
                            )
                        )
                    )
                    add(
                        Event(
                            payload = PhoneCallData(
                                startTime = 1000.let { time += it; time },
                                phone = "+79052345675",
                                isIncoming = true,
                                endTime = time + 10000,
                                isMissed = true
                            )
                        )
                    )
                }
            }
        }
        showComplete()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun onShowConcurrentApps() {
        val sb = StringBuilder()
        val intent = Intent("android.provider.Telephony.SMS_RECEIVED")
        val activities = requireContext().packageManager.queryBroadcastReceivers(intent, 0)
        for (resolveInfo in activities) {
            val activityInfo = resolveInfo.activityInfo
            if (activityInfo != null) {
                sb.append(activityInfo.packageName)
                    .append(" : ")
                    .append(resolveInfo.priority)
                    .append("\n")

                log.debug(
                    "Concurrent package:" + activityInfo.packageName + " priority: " +
                            resolveInfo.priority
                )
            }
        }

        showInfoDialog("Concurrent Apps", sb.toString())
    }

    private fun onGoogleDriveClear(preference: Preference) {
        runBackgroundGoogleDriveTask(preference) {
            GoogleDrive(requireContext(), senderAccount()).clear()
        }
    }

    private fun onGoogleDriveSync(preference: Preference) {
        ConfirmDialog("Synchronize with drive?") {
            runBackgroundGoogleDriveTask(preference) {
                Synchronizer(requireContext(), senderAccount(), database).sync()
            }
        }.show(this)
    }

    private fun onGoogleDriveDownload(preference: Preference) {
        ConfirmDialog("Download from drive?") {
            runBackgroundGoogleDriveTask(preference) {
                Synchronizer(requireContext(), senderAccount(), database).sync(SYNC_FORCE_DOWNLOAD)
            }
        }.show(this)
    }

    private fun onGoogleDriveUpload(preference: Preference) {
        ConfirmDialog("Upload to drive?") {
            runBackgroundGoogleDriveTask(preference) {
                Synchronizer(requireContext(), senderAccount(), database).sync(SYNC_FORCE_UPLOAD)
            }
        }.show(this)
    }

    @SuppressLint("SetTextI18n")
    private fun onSendSms() {
        InputDialog(
            title = "Phone number",
            inputType = InputType.TYPE_CLASS_PHONE,
            initialValue = "5556",
            positiveAction = { value ->
                try {
                    requireContext().sendSmsMessage(value, "Debug message")
                } catch (x: Throwable) {
                    showError("SMS", x)
                }
            }
        )
    }

    private fun onShowAccounts() {
        val s = "Selected: ${senderAccount().name}\n\n" +
                "Service: ${serviceAccount().name}\n\n" +
                "Primary: ${accounts.getPrimaryGoogleAccount()?.name}"
        showInfoDialog("Accounts", s)
    }

    private fun onSendTelegramMessage() {
        TelegramSession(
            context = requireContext(),
            token = settings.getString(PREF_TELEGRAM_BOT_TOKEN)
        ).sendMessage(
            oldChatId = null,
            message = "Debug message",
            messageFormat = "HTML",
            onSuccess = {
                showComplete()
            },
            onError = { error ->
                showError("Telegram", error)
            }
        )
    }

    private fun showComplete() {
        showToast(R.string.operation_complete)
    }

    private fun showError(title: String, error: Throwable) {
        showInfoDialog(title, error.message ?: error.toString())
    }

    private fun senderAccount(): Account {
        return accounts.requireGoogleAccount(settings.getString(PREF_MAIL_SENDER_ACCOUNT))
    }

    private fun serviceAccount(): Account {
        return accounts.requireGoogleAccount(settings.getString(PREF_REMOTE_CONTROL_ACCOUNT))
    }

    private fun runBackgroundGoogleDriveTask(
        preference: Preference,
        onPerform: () -> Unit
    ) {
        preference.runBackgroundTask(
            onPerform,
            onSuccess = {
                showComplete()
            },
            onError = { error ->
                showError("Google drive", error)
            }
        )
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerReceiver(receiver: BroadcastReceiver, filter: IntentFilter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            requireContext().registerReceiver(receiver, filter)
        }
    }

    private fun unregisterReceiver(receiver: BroadcastReceiver) {
        requireContext().unregisterReceiver(receiver)
    }

    private inner class SentStatusReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val message: String = when (resultCode) {
                RESULT_OK ->
                    "Message sent successfully"

                RESULT_ERROR_GENERIC_FAILURE ->
                    "Generic failure error"

                RESULT_ERROR_NO_SERVICE ->
                    "No service available"

                RESULT_ERROR_NULL_PDU ->
                    "Null PDU"

                RESULT_ERROR_RADIO_OFF ->
                    "Radio is off"

                else ->
                    "Unknown error"
            }

            showInfoDialog("SMS", message)
        }
    }

    private inner class DeliveryStatusReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val message = when (resultCode) {
                RESULT_OK ->
                    "Message delivered successfully"

                RESULT_CANCELED ->
                    "Delivery cancelled"

                else ->
                    "Message not delivered"
            }

            showInfoDialog("SMS", message)
        }
    }

    companion object {

        private val log = Logger("ui.DebugFragment")
    }
}