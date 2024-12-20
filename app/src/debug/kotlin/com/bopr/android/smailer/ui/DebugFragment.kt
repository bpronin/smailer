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
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.NotificationsHelper
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_GOOGLE_ACCOUNT
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_MAIL_RECIPIENTS
import com.bopr.android.smailer.NotificationsHelper.Companion.NTF_SERVICE_ACCOUNT
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSAGE_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_MESSAGE_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_DEFAULT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_EVENT_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_CALLS
import com.bopr.android.smailer.Settings.Companion.sharedPreferencesName
import com.bopr.android.smailer.control.MailControlProcessor
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.data.Database.Companion.databaseName
import com.bopr.android.smailer.external.Firebase
import com.bopr.android.smailer.external.Firebase.Companion.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.external.GoogleDrive
import com.bopr.android.smailer.messenger.mail.GoogleMailSession
import com.bopr.android.smailer.messenger.mail.MailMessage
import com.bopr.android.smailer.messenger.telegram.TelegramSession
import com.bopr.android.smailer.provider.EventState.Companion.STATE_IGNORED
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PENDING
import com.bopr.android.smailer.provider.EventState.Companion.STATE_PROCESSED
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.provider.telephony.PhoneEventData.Companion.ACCEPT_STATE_ACCEPTED
import com.bopr.android.smailer.provider.telephony.PhoneEventProcessor
import com.bopr.android.smailer.provider.telephony.PhoneEventProcessorWorker.Companion.startPhoneEventProcessing
import com.bopr.android.smailer.sync.Synchronizer
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_FORCE_DOWNLOAD
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_FORCE_UPLOAD
import com.bopr.android.smailer.ui.InfoDialog.Companion.showInfoDialog
import com.bopr.android.smailer.util.DEVICE_NAME
import com.bopr.android.smailer.util.GeoLocation.Companion.getGeoLocation
import com.bopr.android.smailer.util.PreferenceProgress
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.escapeRegex
import com.bopr.android.smailer.util.getContactName
import com.bopr.android.smailer.util.readLogcatLog
import com.bopr.android.smailer.util.requireIgnoreBatteryOptimization
import com.bopr.android.smailer.util.runBackgroundTask
import com.bopr.android.smailer.util.sendSmsMessage
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.showToast
import com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM
import com.bopr.android.smailer.util.Logger
import java.io.File
import java.lang.System.*

/**
 * For debug purposes.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class DebugFragment : PreferenceFragmentCompat() {

    private lateinit var settings: Settings
    private lateinit var database: Database
    private lateinit var authorization: GoogleAuthorizationHelper
    private lateinit var notifications: NotificationsHelper
    private lateinit var accountHelper: AccountHelper
    private lateinit var smsSendStatusReceiver: BroadcastReceiver
    private lateinit var smsDeliveryStatusReceiver: BroadcastReceiver
    private val developerEmail by lazy { getString(R.string.developer_email) }
    private val firebase by lazy { Firebase(requireContext()) }

    //    private val requestPermissionLauncher =
//        registerForActivityResult(RequestPermission()) { result: Boolean ->
//            onPermissionRequestResult(result)
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesName = sharedPreferencesName

        settings = Settings(requireContext())
        database = Database(requireContext())
        authorization = GoogleAuthorizationHelper(
            requireActivity(), PREF_EMAIL_SENDER_ACCOUNT, MAIL_GOOGLE_COM, DRIVE_APPDATA
        )
        notifications = NotificationsHelper(requireContext())
        accountHelper = AccountHelper(requireContext())

        smsSendStatusReceiver = SentStatusReceiver().also {
            registerReceiver(it, IntentFilter("SMS_SENT"))
        }

        smsDeliveryStatusReceiver = DeliveryStatusReceiver().also {
            registerReceiver(it, IntentFilter("SMS_DELIVERED"))
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        /* do not use fragment's context. see: https://developer.android.com/guide/topics/ui/settings/programmatic-hierarchy*/
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)
        addCategory(screen, "Event processing",
            addPreference("Process single event") {
                onProcessSingleEvent()
            },
            addPreference("Process pending events") {
                onProcessPendingEvents(it)
            },
            addPreference("Send SMS") {
                onSendSms()
            }
        )
        addCategory(screen, "Telegram",
            addPreference("Send debug message") {
                onSendTelegramMessage()
            }
        )
        addCategory(screen, "Email",
            addPreference("Send debug mail") {
                onSendDebugMail(it)
            }
        )
        addCategory(screen, "Settings",
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
            addPreference("Require battery optimisation disabled") {
                if (!requireActivity().requireIgnoreBatteryOptimization()) {
                    showInfoDialog("Battery", "Optimization already disabled")
                }
            }
        )
        addCategory(screen, "Google drive",
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
        addCategory(screen, "Firebase",
            addPreference("Send message") {
                firebase.send(FCM_REQUEST_DATA_SYNC)
            },
            addPreference("Subscribe") {
                firebase.subscribe()
            },
            addPreference("Unsubscribe") {
                firebase.unsubscribe()
            },
            addPreference("Get current token") {
                firebase.requestToken { token ->
                    showInfoDialog("Firebase token", token)
                }
            },
            addPreference("Get server info") {
                firebase.requestInfo { info ->
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
                requireContext().deleteDatabase(databaseName)
                showComplete()
            }
        )
        addCategory(screen, "Permissions",
            addPreference("Request gmail api permission") {
                onRequestGooglePermission()
            }
        )
        addCategory(screen, "Logging",
            addPreference("Send logs to developer") {
                onSendLog(it)
            },
            addPreference("Clear logs") {
                onClearLogs()
            }
        )
        addCategory(screen, "Notifications",
            addPreference("Cancel errors") {
                notifications.cancelError(NTF_GOOGLE_ACCOUNT)
                notifications.cancelError(NTF_SERVICE_ACCOUNT)
                notifications.cancelError(NTF_MAIL_RECIPIENTS)
            }
        )
        addCategory(screen, "Email remote control",
            addPreference("Process service mail") {
                onProcessServiceMail(it)
            }
        )
        addCategory(screen, "Other",
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
            putString(PREF_EMAIL_SENDER_ACCOUNT, "unknown@gmail.com")
        }
    }

    private fun onSendLog(preference: Preference) {
        val progress = PreferenceProgress(preference).apply { start() }
        val context = requireContext()

        val attachments: MutableList<File> = mutableListOf()
        attachments.add(context.getDatabasePath(databaseName))
        attachments.add(context.readLogcatLog())

        File(context.filesDir, "log").listFiles()?.let {
            attachments.addAll(it)
        }

        val account = accountHelper.requirePrimaryGoogleAccount()
        val session = GoogleMailSession(context, account, GMAIL_SEND)
        for (file in attachments) {
            val message = MailMessage(
                subject = "[SMailer] log: " + file.name,
                from = account.name,
                body = "Device: " + DEVICE_NAME + "<br>File: " + file.name,
                attachment = setOf(file),
                recipients = developerEmail
            )

            session.send(message,
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
        val account = accountHelper.requirePrimaryGoogleAccount()

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
            putString(PREF_EMAIL_SENDER_ACCOUNT, developerEmail)
            putString(PREF_REMOTE_CONTROL_ACCOUNT, developerEmail)
            putStringList(
                PREF_EMAIL_MESSENGER_RECIPIENTS,
                setOf(developerEmail, "nowhere@mail.com")
            )
            putStringSet(
                PREF_EMAIL_TRIGGERS, mutableSetOf(
                    VAL_PREF_TRIGGER_IN_SMS,
                    VAL_PREF_TRIGGER_IN_CALLS,
                    VAL_PREF_TRIGGER_MISSED_CALLS,
                    VAL_PREF_TRIGGER_OUT_CALLS,
                )
            )
            putStringSet(
                PREF_EMAIL_MESSAGE_CONTENT, mutableSetOf(
                    VAL_PREF_MESSAGE_CONTENT_CALLER,
                    VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
                    VAL_PREF_MESSAGE_CONTENT_LOCATION,
                    VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME,
                    VAL_PREF_MESSAGE_CONTENT_HEADER,
                    VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS,
                    VAL_PREF_MESSAGE_CONTENT_EVENT_TIME
                )
            )
            putString(PREF_MESSAGE_LOCALE, VAL_PREF_DEFAULT)
            putBoolean(PREF_NOTIFY_SEND_SUCCESS, true)
        }

        database.phoneBlacklist.replaceAll(setOf("+123456789", "+9876543*"))
        database.smsTextBlacklist.replaceAll(setOf("Bad text", escapeRegex("Expression")))

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
        authorization.startAccountPicker()
    }

    private fun onProcessSingleEvent() {
        val start = currentTimeMillis()
        val info = PhoneEventData(
            phone = "+1(234) 567-89-01",
            isIncoming = true,
            startTime = start,
            endTime = start + 10000,
            isMissed = false,
            text = "Debug SMS message text",
            location = null,
            details = null,
            acceptor = DEVICE_NAME,
            acceptState = ACCEPT_STATE_ACCEPTED,
            isRead = false
        )
        requireContext().startPhoneEventProcessing(info)
        showComplete()
    }

    private fun onProcessPendingEvents(preference: Preference) {
        preference.runBackgroundTask(
            onPerform = {
                PhoneEventProcessor(requireContext()).processPending()
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
                PhoneEventData(
                    "+79052345670",
                    true,
                    currentTimeMillis(),
                    null,
                    false,
                    "Debug message",
                    null,
                    null,
                    STATE_PENDING,
                    DEVICE_NAME,
                    ACCEPT_STATE_ACCEPTED,
                    isRead = false
                )
            )
        }
        showComplete()
    }

    private fun onPopulateHistory() {
        var time = currentTimeMillis()
        val recipient = DEVICE_NAME
        database.commit {
            batch {
                events.add(
                    PhoneEventData(
                        "+79052345671",
                        true,
                        time,
                        null,
                        false,
                        "Debug message",
                        null,
                        null,
                        STATE_PENDING,
                        recipient,
                        ACCEPT_STATE_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEventData(
                        "+79052345672",
                        false,
                        1000.let { time += it; time },
                        null,
                        false,
                        "Debug message",
                        null,
                        null,
                        STATE_PROCESSED,
                        recipient,
                        ACCEPT_STATE_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEventData(
                        "+79052345673",
                        true,
                        1000.let { time += it; time },
                        time + 10000,
                        false,
                        null,
                        null,
                        null,
                        STATE_IGNORED,
                        recipient,
                        ACCEPT_STATE_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEventData(
                        "+79052345674",
                        false,
                        1000.let { time += it; time },
                        time + 10000,
                        false,
                        null,
                        null,
                        null,
                        STATE_PENDING,
                        recipient,
                        ACCEPT_STATE_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEventData(
                        "+79052345675",
                        true,
                        1000.let { time += it; time },
                        time + 10000,
                        true,
                        null,
                        null,
                        null,
                        STATE_PENDING,
                        recipient,
                        ACCEPT_STATE_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEventData(
                        "+79052345671",
                        true,
                        1000.let { time += it; time },
                        null,
                        false,
                        "Debug message",
                        null,
                        "Test exception +79052345671",
                        STATE_PENDING,
                        recipient,
                        ACCEPT_STATE_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEventData(
                        "+79052345672",
                        false,
                        1000.let { time += it; time },
                        null,
                        false,
                        "Debug message",
                        null,
                        "Test exception +79052345672",
                        STATE_PENDING,
                        recipient,
                        ACCEPT_STATE_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEventData(
                        "+79052345673",
                        true,
                        1000.let { time += it; time },
                        time + 10000,
                        false,
                        null,
                        null,
                        "Test exception +79052345673",
                        STATE_PENDING,
                        recipient,
                        ACCEPT_STATE_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEventData(
                        "+79052345674",
                        false,
                        1000.let { time += it; time },
                        time + 10000,
                        false,
                        null,
                        null,
                        "Test exception +79052345674",
                        STATE_PENDING,
                        recipient,
                        ACCEPT_STATE_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEventData(
                        "+79052345675",
                        true,
                        1000.let { time += it; time },
                        time + 10000,
                        true,
                        null,
                        null,
                        "Test exception +79052345675",
                        STATE_PENDING,
                        recipient,
                        ACCEPT_STATE_ACCEPTED,
                        isRead = false
                    )
                )
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
                "Primary: ${accountHelper.getPrimaryGoogleAccount()?.name}"
        showInfoDialog("Accounts", s)
    }

    private fun onSendTelegramMessage() {
        TelegramSession(
            context = requireContext(),
            token = settings.getString(PREF_TELEGRAM_BOT_TOKEN)
        ).sendMessage(
            oldChatId = null,
            message = "Debug message",
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
        return accountHelper.requireGoogleAccount(settings.getString(PREF_EMAIL_SENDER_ACCOUNT))
    }

    private fun serviceAccount(): Account {
        return accountHelper.requireGoogleAccount(settings.getString(PREF_REMOTE_CONTROL_ACCOUNT))
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