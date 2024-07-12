package com.bopr.android.smailer.ui

import android.Manifest.permission.READ_CONTACTS
import android.accounts.Account
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager.RESULT_ERROR_GENERIC_FAILURE
import android.telephony.SmsManager.RESULT_ERROR_NO_SERVICE
import android.telephony.SmsManager.RESULT_ERROR_NULL_PDU
import android.telephony.SmsManager.RESULT_ERROR_RADIO_OFF
import android.telephony.SmsManager.getDefault
import android.text.InputType
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.bopr.android.smailer.CallProcessor
import com.bopr.android.smailer.CallProcessorService.Companion.startCallProcessingService
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.Database.Companion.databaseName
import com.bopr.android.smailer.GeoLocator
import com.bopr.android.smailer.GoogleMail
import com.bopr.android.smailer.MailMessage
import com.bopr.android.smailer.Notifications
import com.bopr.android.smailer.Notifications.Companion.RECIPIENTS_ERROR
import com.bopr.android.smailer.Notifications.Companion.REMOTE_ACCOUNT_ERROR
import com.bopr.android.smailer.Notifications.Companion.SENDER_ACCOUNT_ERROR
import com.bopr.android.smailer.Notifications.Companion.TARGET_PHONE_BLACKLIST
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_DEFAULT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_CONTACT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_LOW_BATTERY_LEVEL
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_CALLS
import com.bopr.android.smailer.firebase.CloudMessaging.FCM_REQUEST_DATA_SYNC
import com.bopr.android.smailer.firebase.CloudMessaging.listFirebaseInfo
import com.bopr.android.smailer.firebase.CloudMessaging.requestFirebaseToken
import com.bopr.android.smailer.firebase.CloudMessaging.sendCloudMessage
import com.bopr.android.smailer.firebase.CloudMessaging.subscribeToCloudMessaging
import com.bopr.android.smailer.firebase.CloudMessaging.unsubscribeFromCloudMessaging
import com.bopr.android.smailer.remote.RemoteControlProcessor
import com.bopr.android.smailer.sync.GoogleDrive
import com.bopr.android.smailer.sync.Synchronizer
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_FORCE_DOWNLOAD
import com.bopr.android.smailer.sync.Synchronizer.Companion.SYNC_FORCE_UPLOAD
import com.bopr.android.smailer.ui.BatteryOptimizationHelper.isIgnoreBatteryOptimizationRequired
import com.bopr.android.smailer.ui.BatteryOptimizationHelper.requireIgnoreBatteryOptimization
import com.bopr.android.smailer.util.checkPermission
import com.bopr.android.smailer.util.contactName
import com.bopr.android.smailer.util.deviceName
import com.bopr.android.smailer.util.escapeRegex
import com.bopr.android.smailer.util.getAccount
import com.bopr.android.smailer.util.primaryAccount
import com.bopr.android.smailer.util.readLogcatLog
import com.bopr.android.smailer.util.runInBackground
import com.bopr.android.smailer.util.runLongTask
import com.bopr.android.smailer.util.showToast
import com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.GmailScopes.MAIL_GOOGLE_COM
import org.slf4j.LoggerFactory
import java.io.File

/**
 * For debug purposes.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class DebugFragment : BasePreferenceFragment() {

    private lateinit var locator: GeoLocator
    private lateinit var database: Database
    private lateinit var authorizator: GoogleAuthorizationHelper
    private lateinit var notifications: Notifications
    private lateinit var sentStatusReceiver: BroadcastReceiver
    private lateinit var deliveredStatusReceiver: BroadcastReceiver

    private val developerEmail: String
        get() = getString(R.string.developer_email)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        /* do not use fragment's context. see: https://developer.android.com/guide/topics/ui/settings/programmatic-hierarchy*/
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)
        addCategory(screen, "Call processing",
            addPreference("Process single event") {
                onProcessSingleEvent()
            },
            addPreference("Process pending events") { preference ->
                onStartProcessPendingEvents(preference)
            },
            addPreference("Process service mail") { preference ->
                onProcessServiceMail(preference)
            },
            addPreference("Send debug mail") { preference ->
                onSendDebugMail(preference)
            },
            addPreference("Send SMS") {
                onSendSms()
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
                if (isIgnoreBatteryOptimizationRequired(context)) {
                    requireIgnoreBatteryOptimization(requireActivity())
                } else {
                    showToast("Battery optimization already disabled")
                }
            }
        )
        addCategory(screen, "Google drive",
            addPreference("Download from drive") { preference ->
                onGoogleDriveDownload(preference)
            },
            addPreference("Upload to drive") { preference ->
                onGoogleDriveUpload(preference)
            },
            addPreference("Clear remote data") { preference ->
                onGoogleDriveClear(preference)
            },
            addPreference("Sync data") { preference ->
                onGoogleDriveSync(preference)
            }
        )
        addCategory(screen, "Firebase",
            addPreference("Send message") {
                requireContext().sendCloudMessage(FCM_REQUEST_DATA_SYNC)
            },
            addPreference("Subscribe") {
                requireContext().subscribeToCloudMessaging()
            },
            addPreference("Unsubscribe") {
                unsubscribeFromCloudMessaging()
            },
            addPreference("Get current token") {
                requestFirebaseToken { token ->
                    showInfo("Firebase token", token)
                }
            },
            addPreference("Get server info") {
                requireContext().listFirebaseInfo {
                    showInfo("Info", it)
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
                showSuccess()
            },
            addPreference("Mark all as read") {
                database.commit { batch { events.markAllAsRead(true) } }
                showSuccess()
            },
            addPreference("Clear calls log") {
                database.commit { batch { events.clear() } }
                showSuccess()
            },
            addPreference("Destroy database") {
                requireContext().deleteDatabase(databaseName)
                showSuccess()
            }
        )
        addCategory(screen, "Permissions",
            addPreference("Request gmail api permission") {
                onRequestGooglePermission()
            }
        )
        addCategory(screen, "Logging",
            addPreference("Send logs to developer") { preference ->
                onSendLog(preference)
            },
            addPreference("Clear logs") {
                onClearLogs()
            }
        )
        addCategory(screen, "Notifications",
            addPreference("Show sender error") {
                notifications.showSenderAccountError()
            },
            addPreference("Show recipients error") {
                notifications.showRecipientsError(R.string.no_recipients_specified)
            },
            addPreference("Show mail success") {
                notifications.showMailSendSuccess()
            },
            addPreference("Show remote action") {
                notifications.showRemoteAction(
                    getString(R.string.text_remotely_added_to_blacklist, "spam text"),
                    TARGET_PHONE_BLACKLIST
                )
            },
            addPreference("Cancel errors") {
                notifications.cancelError(SENDER_ACCOUNT_ERROR)
                notifications.cancelError(REMOTE_ACCOUNT_ERROR)
                notifications.cancelError(RECIPIENTS_ERROR)
            }
        )
        addCategory(screen, "Other",
            addPreference("Get location") {
                onGetLocation()
            },
            addPreference("Get contact") {
                onGetContact()
            },
            addPreference("Show accounts") {
                onShowAccounts()
            },
            addPreference("Show concurrent applications") {
                onShowConcurrent()
            },
            addPreference("Crash!") {
                throw RuntimeException("Test crash")
            }
        )
        preferenceScreen = screen
    }

    private fun onPutInvalidAccount() {
        settings.update {
            putString(PREF_SENDER_ACCOUNT, "unknown@gmail.com")
        }
    }

    private fun onSendLog(preference: Preference) {
        runLongTask(preference) {
            val context = requireContext()

            val attachments: MutableList<File> = mutableListOf()
            attachments.add(context.getDatabasePath(databaseName))
            attachments.add(context.readLogcatLog())

            File(context.filesDir, "log").listFiles()?.let {
                attachments.addAll(it)
            }

            val account = context.primaryAccount()!!
            val transport = GoogleMail(context)

            transport.login(account, GmailScopes.GMAIL_SEND)
            for (file in attachments) {
                val message = MailMessage(
                    subject = "[SMailer] log: " + file.name,
                    from = account.name,
                    body = "Device: " + deviceName() + "<br>File: " + file.name,
                    attachment = setOf(file),
                    recipients = developerEmail
                )
                transport.send(message)
            }

        }
    }

    private fun onSendDebugMail(preference: Preference) {
        runLongTask(preference) {
            val account = requireContext().primaryAccount()!!

            val message = MailMessage(
                from = account.name,
                subject = "test subject",
                body = "test message from " + deviceName(),
                recipients = developerEmail
            )

            GoogleMail(requireContext()).run {
                login(account, GmailScopes.GMAIL_SEND)
                send(message)
            }
        }
    }

    private fun onGetLocation() {
        runInBackground({
            locator.getLocation()
        }, { result, _ ->
            showInfo("Location", result?.format() ?: "No location received")
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = requireContext()

        database = Database(context)
        locator = GeoLocator(context, database)
        authorizator =
            GoogleAuthorizationHelper(this, PREF_SENDER_ACCOUNT, MAIL_GOOGLE_COM, DRIVE_APPDATA)
        notifications = Notifications(context)
        sentStatusReceiver = SentStatusReceiver()
        deliveredStatusReceiver = DeliveryStatusReceiver()
        context.registerReceiver(sentStatusReceiver, IntentFilter("SMS_SENT"))
        context.registerReceiver(deliveredStatusReceiver, IntentFilter("SMS_DELIVERED"))
    }

    override fun onDestroy() {
        val context = requireContext()
        context.unregisterReceiver(sentStatusReceiver)
        context.unregisterReceiver(deliveredStatusReceiver)
        database.close()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authorizator.onAccountSelectorActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_RECEIVE_SMS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showInfo("Permission", "Granted")
            } else {
                showInfo("Permission", "Denied")
            }
        }
    }

    private fun addPreference(title: String, onClick: (Preference) -> Unit): Preference {
        return Preference(requireContext()).apply {
            setIcon(R.drawable.ic_bullet)
            this.title = title
            onPreferenceClickListener = object : DefaultClickListener() {

                override fun onClick(preference: Preference) {
                    onClick(preference)
                }

            }
        }
    }

    private fun addCategory(
        screen: PreferenceScreen,
        title: String,
        vararg preferences: Preference
    ) {
        val category = PreferenceCategory(requireContext())
        screen.addPreference(category)
        category.title = title
        for (preference in preferences) {
            category.addPreference(preference)
        }
    }

    private fun onSetDebugPreferences() {
        settings.update {
            putString(PREF_SENDER_ACCOUNT, developerEmail)
            putString(PREF_REMOTE_CONTROL_ACCOUNT, developerEmail)
            putStringList(PREF_RECIPIENTS_ADDRESS, setOf(developerEmail, "nowhere@mail.com"))
            putStringSet(
                PREF_EMAIL_TRIGGERS, mutableSetOf(
                    VAL_PREF_TRIGGER_IN_SMS,
                    VAL_PREF_TRIGGER_IN_CALLS,
                    VAL_PREF_TRIGGER_MISSED_CALLS,
                    VAL_PREF_TRIGGER_OUT_CALLS,
                    VAL_PREF_LOW_BATTERY_LEVEL
                )
            )
            putStringSet(
                PREF_EMAIL_CONTENT, mutableSetOf(
                    VAL_PREF_EMAIL_CONTENT_CONTACT,
                    VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                    VAL_PREF_EMAIL_CONTENT_LOCATION,
                    VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT,
                    VAL_PREF_EMAIL_CONTENT_HEADER,
                    VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS,
                    VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME
                )
            )
            putString(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT)
            putBoolean(PREF_NOTIFY_SEND_SUCCESS, true)
        }

        database.phoneBlacklist.replaceAll(setOf("+123456789", "+9876543*"))
        database.textBlacklist.replaceAll(setOf("Bad text", escapeRegex("Expression")))

        showSuccess()
    }

    private fun onGetContact() {
        if (checkPermission(READ_CONTACTS)) {
            InputDialog(
                title = "Phone number",
                inputType = InputType.TYPE_CLASS_PHONE,
                positiveAction = {
                    val contact = contactName(requireContext(), it)
                    val text = if (contact != null) "$it: $contact" else "Contact not found"
                    showToast(text)
                }
            ).show(this)
        } else {
            showToast("Missing required permission")
        }
    }

    private fun onClearPreferences() {
        settings.update { clear() }
        showSuccess()
    }

    private fun onResetPreferences() {
        settings.loadDefaults()
        showSuccess()
    }

    private fun onProcessServiceMail(preference: Preference) {
        if (settings.isRemoteControlEnabled) {
            runLongTask(preference) {
                RemoteControlProcessor(requireContext()).checkMailbox()
            }
        } else {
            showToast("Feature disabled")
        }
    }

    private fun onRequestGooglePermission() {
        authorizator.startAccountSelectorActivity()
    }

    private fun onProcessSingleEvent() {
        val start = System.currentTimeMillis()
        val event = PhoneEvent(
            phone = "+1(234) 567-89-01",
            isIncoming = true,
            startTime = start,
            endTime = start + 10000,
            isMissed = false,
            text = "debug SMS message text",
            location = null,
            details = null,
            state = STATE_PENDING,
            acceptor = deviceName(),
            processStatus = STATUS_ACCEPTED,
            isRead = false
        )
        startCallProcessingService(requireContext(), event)
        showSuccess()
    }

    private fun onStartProcessPendingEvents(preference: Preference) {
        runLongTask(preference) {
            CallProcessor(requireContext()).processPending()
        }
    }

    private fun onClearLogs() {
        val dir = File(requireContext().filesDir, "log")
        val logs = dir.listFiles()!!
        for (file in logs) {
            if (!file.delete()) {
                log.warn("Cannot delete file")
            }
        }
        showToast("Removed ${logs.size} log files")
    }

    private fun onAddHistoryItem() {
        database.commit {
            events.add(
                PhoneEvent(
                    "+79052345670",
                    true,
                    System.currentTimeMillis(),
                    null,
                    false,
                    "Debug message",
                    null,
                    null,
                    STATE_PENDING,
                    deviceName(),
                    STATUS_ACCEPTED,
                    isRead = false
                )
            )
        }
        showSuccess()
    }

    private fun onPopulateHistory() {
        var time = System.currentTimeMillis()
        val recipient = deviceName()
        database.commit {
            batch {
                events.add(
                    PhoneEvent(
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
                        STATUS_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEvent(
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
                        STATUS_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEvent(
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
                        STATUS_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEvent(
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
                        STATUS_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEvent(
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
                        STATUS_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEvent(
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
                        STATUS_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEvent(
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
                        STATUS_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEvent(
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
                        STATUS_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEvent(
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
                        STATUS_ACCEPTED,
                        isRead = false
                    )
                )
                events.add(
                    PhoneEvent(
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
                        STATUS_ACCEPTED,
                        isRead = false
                    )
                )
            }
        }
        showSuccess()
    }

    private fun onShowConcurrent() {
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

        showInfo("Concurrents", sb.toString())
    }

    private fun onGoogleDriveClear(preference: Preference) {
        runLongTask(preference) {
            val drive = GoogleDrive(requireContext())
            drive.login(senderAccount())
            drive.clear()
        }
    }

    private fun onGoogleDriveSync(preference: Preference) {
        ConfirmDialog("Synchronize with drive?") {
            runLongTask(preference) {
                Synchronizer(requireContext(), senderAccount(), database).sync()
            }
        }.show(this)
    }

    private fun onGoogleDriveDownload(preference: Preference) {
        ConfirmDialog("Download from drive?") {
            runLongTask(preference) {
                Synchronizer(requireContext(), senderAccount(), database).sync(SYNC_FORCE_DOWNLOAD)
            }
        }.show(this)
    }

    private fun onGoogleDriveUpload(preference: Preference) {
        ConfirmDialog("Upload to drive?") {
            runLongTask(preference) {
                Synchronizer(requireContext(), senderAccount(), database).sync(SYNC_FORCE_UPLOAD)
            }
        }.show(this)
    }

    @SuppressLint("SetTextI18n")
    private fun onSendSms() {
        InputDialog(title = "Phone number",
            inputType = InputType.TYPE_CLASS_PHONE,
            initialValue = "5556",
            positiveAction = {
                val sentIntent = PendingIntent.getBroadcast(
                    requireContext(), 0, Intent("SMS_SENT"),
                    PendingIntent.FLAG_IMMUTABLE
                )
                val deliveredIntent = PendingIntent.getBroadcast(
                    requireContext(), 0, Intent("SMS_DELIVERED"),
                    PendingIntent.FLAG_IMMUTABLE
                )
                try {
                    getDefault().sendTextMessage(
                        it,
                        null,
                        "Debug message",
                        sentIntent,
                        deliveredIntent
                    )
                } catch (x: Throwable) {
                    log.error("Failed: ", x)
                    showToast("Failed")
                }
            }
        )
    }

    private fun onShowAccounts() {
        val s = "Selected: ${senderAccount().name}\n\n" +
                "Service: ${serviceAccount().name}\n\n" +
                "Primary: ${requireContext().primaryAccount()?.name}"
        showInfo("Accounts", s)
    }

    private fun showSuccess() {
        showToast(R.string.operation_complete)
    }

    private fun showError(error: Throwable) {
        showInfo("Error", error.message ?: error.toString())
    }

    private fun showInfo(title: String, message: String) {
        InfoDialog(title, message).show(requireActivity())
    }

    private fun senderAccount(): Account {
        return requireContext().getAccount(settings.senderAccount)!!
    }

    private fun serviceAccount(): Account {
        return requireContext().getAccount(settings.remoteControlAccount)!!
    }

    private fun runLongTask(preference: Preference, onPerform: () -> Unit) {
        preference.runLongTask(
            onPerform = onPerform,
            onComplete = { _, error ->
                if (error != null) showError(error) else showSuccess()
            })
    }

    private abstract inner class DefaultClickListener : Preference.OnPreferenceClickListener {

        protected abstract fun onClick(preference: Preference)

        override fun onPreferenceClick(preference: Preference): Boolean {
            onClick(preference)
            return true
        }
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

            showToast(message)
            log.debug(message)
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

            showToast(message)
            log.debug(message)
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger("DebugFragment")
        private const val PERMISSIONS_REQUEST_RECEIVE_SMS = 100
    }
}