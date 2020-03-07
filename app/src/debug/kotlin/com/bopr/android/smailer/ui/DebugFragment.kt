package com.bopr.android.smailer.ui

import android.Manifest.permission.READ_CONTACTS
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
import android.telephony.SmsManager.*
import android.text.InputType
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.bopr.android.smailer.*
import com.bopr.android.smailer.CallProcessorService.Companion.startCallProcessingService
import com.bopr.android.smailer.Notifications.Companion.TARGET_MAIN
import com.bopr.android.smailer.Notifications.Companion.TARGET_RULES
import com.bopr.android.smailer.PendingCallProcessorWorker.Companion.startPendingCallProcessorService
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_IGNORED
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PENDING
import com.bopr.android.smailer.PhoneEvent.Companion.STATE_PROCESSED
import com.bopr.android.smailer.PhoneEvent.Companion.STATUS_ACCEPTED
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_LOCALE
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_NOTIFY_SEND_SUCCESS
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ENABLED
import com.bopr.android.smailer.Settings.Companion.PREF_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_DEFAULT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_CONTACT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_DEVICE_NAME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_HEADER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_CALLS
import com.bopr.android.smailer.remote.RemoteControlWorker.Companion.startRemoteControlService
import com.bopr.android.smailer.sync.Synchronizer
import com.bopr.android.smailer.ui.BatteryOptimizationHelper.isIgnoreBatteryOptimizationRequired
import com.bopr.android.smailer.ui.BatteryOptimizationHelper.requireIgnoreBatteryOptimization
import com.bopr.android.smailer.util.*
import com.google.android.gms.tasks.Tasks
import com.google.api.services.drive.DriveScopes
import com.google.api.services.gmail.GmailScopes
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * For debug purposes.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class DebugFragment : BasePreferenceFragment() {

    private lateinit var appContext: Context
    private lateinit var locator: GeoLocator
    private lateinit var database: Database
    private lateinit var authorizator: GoogleAuthorizationHelper
    private lateinit var notifications: Notifications
    private lateinit var sentStatusReceiver: BroadcastReceiver
    private lateinit var deliveredStatusReceiver: BroadcastReceiver

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        /* do not use fragment's context. see: https://developer.android.com/guide/topics/ui/settings/programmatic-hierarchy*/
        appContext = preferenceManager.context

        val screen = preferenceManager.createPreferenceScreen(appContext)
        addCategory(screen, "Call processing",
                createPreference("Process single event", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onProcessSingleEvent()
                    }
                }),
                createPreference("Process pending events", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onStartProcessPendingEvents()
                    }
                }),
                createPreference("Process service mail", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onProcessServiceMail()
                    }
                }),
                createPreference("Send debug mail", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        SendDebugMailTask(requireActivity(), loadDebugProperties()).execute()
                    }
                }),
                createPreference("Send SMS", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onSendSms()
                    }
                })
        )
        addCategory(screen, "Settings",
                createPreference("Populate settings", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onSetDebugPreferences()
                    }
                }),
                createPreference("Clear settings", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onClearPreferences()
                    }
                }),
                createPreference("Load default settings", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onResetPreferences()
                    }
                }),
                createPreference("Put invalid account", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        settings.update { putString(PREF_SENDER_ACCOUNT, "unknown@gmail.com") }
                    }
                }),
                createPreference("Require battery optimisation disabled", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        if (isIgnoreBatteryOptimizationRequired(requireContext())) {
                            requireIgnoreBatteryOptimization(requireActivity())
                        } else {
                            showToast("Battery optimization already ignored")
                        }
                    }
                })
        )
        addCategory(screen, "Google drive",
                createPreference("Download from drive", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onGoogleDriveDownload()
                    }
                }),
                createPreference("Upload to drive", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onGoogleDriveUpload()
                    }
                }),
                createPreference("Clear remote data", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onGoogleDriveClear()
                    }
                }),
                createPreference("Sync data", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onGoogleDriveSync()
                    }
                })
        )
        addCategory(screen, "Database",
                createPreference("Add an item to calls log", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onAddHistoryItem()
                    }
                }),
                createPreference("Add 10 items to calls log", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onPopulateHistory()
                    }
                }),
                createPreference("Mark all as unread", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        database.markAllAsRead(false)
                        database.notifyChanged()
                        showToast(R.string.operation_complete)
                    }
                }),
                createPreference("Mark all as read", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        database.markAllAsRead(true)
                        database.notifyChanged()
                        showToast(R.string.operation_complete)
                    }
                }),
                createPreference("Clear calls log", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        database.clearEvents()
                        database.notifyChanged()
                        showToast(R.string.operation_complete)
                    }
                }),
                createPreference("Destroy database", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        database.destroy()
                        showToast(R.string.operation_complete)
                    }
                })
        )
        addCategory(screen, "Permissions",
                createPreference("Request gmail api permission", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onRequestGooglePermission()
                    }
                })
        )
        addCategory(screen, "Logging",
                createPreference("Send logs to developer", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        SendLogTask(requireActivity(), loadDebugProperties()).execute()
                    }
                }),
                createPreference("Clear logs", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onClearLogs()
                    }
                })
        )
        addCategory(screen, "Notifications",
                createPreference("Show error", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        notifications.showMailError(R.string.no_recipients_specified,
                                TARGET_RULES)
                    }
                }),
                createPreference("Show success", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        notifications.showMessage(R.string.email_successfully_send, TARGET_MAIN)
                    }
                }),
                createPreference("Show remote action", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        notifications.showRemoteAction(
                                getString(R.string.text_remotely_added_to_blacklist, "spam text"))
                    }
                }),
                createPreference("Hide errors", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        notifications.cancelAllErrors()
                    }
                })
        )
        addCategory(screen, "Other",
                createPreference("Get location", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        GetLocationTask(requireActivity(), locator).execute()
                    }
                }),
                createPreference("Get contact", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onGetContact()
                    }
                }),
                createPreference("Show accounts", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onShowAccounts()
                    }
                }),
                createPreference("Show concurrent applications", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        onShowConcurrent()
                    }
                }),
                createPreference("Crash!", object : DefaultClickListener() {
                    override fun onClick(preference: Preference) {
                        throw RuntimeException("Test crash")
                    }
                })
        )
        preferenceScreen = screen
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)

        database = Database(appContext)
        locator = GeoLocator(appContext, database)
        authorizator = GoogleAuthorizationHelper(this, PREF_SENDER_ACCOUNT, GmailScopes.MAIL_GOOGLE_COM,
                DriveScopes.DRIVE_APPDATA)
        notifications = Notifications(appContext)
        sentStatusReceiver = SentStatusReceiver()
        deliveredStatusReceiver = DeliveryStatusReceiver()
        appContext.registerReceiver(sentStatusReceiver, IntentFilter("SMS_SENT"))
        appContext.registerReceiver(deliveredStatusReceiver, IntentFilter("SMS_DELIVERED"))
    }

    override fun onDestroy() {
        appContext.unregisterReceiver(sentStatusReceiver)
        appContext.unregisterReceiver(deliveredStatusReceiver)
        database.close()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        authorizator.onAccountSelectorActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_RECEIVE_SMS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                InfoDialog(message = "Permission granted").show(this)
            } else {
                InfoDialog(message = "Permission denied").show(this)
            }
        }
    }

    private fun loadDebugProperties(): Properties {
        val properties = Properties()
        try {
            val stream = appContext.assets.open("debug.properties")
            properties.load(stream)
            stream.close()
        } catch (x: IOException) {
            log.error("Cannot read debug properties", x)
        }
        return properties
    }

    private fun createPreference(title: String, listener: Preference.OnPreferenceClickListener): Preference {
        val preference = Preference(appContext)
        preference.title = title
        preference.setIcon(R.drawable.ic_bullet)
        preference.onPreferenceClickListener = listener
        return preference
    }

    private fun addCategory(screen: PreferenceScreen, title: String, vararg preferences: Preference) {
        val category = PreferenceCategory(appContext)
        screen.addPreference(category)
        category.title = title
        for (preference in preferences) {
            category.addPreference(preference)
        }
    }

    private fun onSetDebugPreferences() {
        val properties = loadDebugProperties()
        settings.update {
            putString(PREF_SENDER_ACCOUNT, primaryAccount(appContext)?.name)
            putString(PREF_REMOTE_CONTROL_ACCOUNT, properties.getProperty("remote_control_account"))
            putCommaSet(PREF_RECIPIENTS_ADDRESS, setOf(properties.getProperty("default_recipient"), "nowhere@mail.com"))
            putStringSet(PREF_EMAIL_TRIGGERS, mutableSetOf(
                    VAL_PREF_TRIGGER_IN_SMS,
                    VAL_PREF_TRIGGER_IN_CALLS,
                    VAL_PREF_TRIGGER_MISSED_CALLS,
                    VAL_PREF_TRIGGER_OUT_CALLS))
            putStringSet(PREF_EMAIL_CONTENT, mutableSetOf(
                    VAL_PREF_EMAIL_CONTENT_CONTACT,
                    VAL_PREF_EMAIL_CONTENT_DEVICE_NAME,
                    VAL_PREF_EMAIL_CONTENT_LOCATION,
                    VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME_SENT,
                    VAL_PREF_EMAIL_CONTENT_HEADER,
                    VAL_PREF_EMAIL_CONTENT_REMOTE_COMMAND_LINKS,
                    VAL_PREF_EMAIL_CONTENT_MESSAGE_TIME))
            putString(PREF_EMAIL_LOCALE, VAL_PREF_DEFAULT)
            putBoolean(PREF_NOTIFY_SEND_SUCCESS, true)
            putString(PREF_FILTER_PHONE_BLACKLIST, commaJoin(setOf("+123456789", "+9876543*")))
            putString(PREF_FILTER_TEXT_BLACKLIST, commaJoin(setOf("Bad text", escapeRegex("Expression"))))
        }
        showToast(R.string.operation_complete)
    }

    private fun onGetContact() {
        if (checkPermission(READ_CONTACTS)) {
            InputDialog(
                    title = "Phone number",
                    inputType = InputType.TYPE_CLASS_PHONE,
                    positiveAction = {
                        val contact = contactName(appContext, it)
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
        showToast(R.string.operation_complete)
    }

    private fun onResetPreferences() {
        settings.loadDefaults()
        showToast(R.string.operation_complete)
    }

    private fun onProcessServiceMail() {
        if (settings.getBoolean(PREF_REMOTE_CONTROL_ENABLED)) {
            startRemoteControlService(appContext)
            showToast(R.string.operation_complete)
        } else {
            showToast("Feature disabled")
        }
    }

    private fun onRequestGooglePermission() {
        authorizator.startAccountSelectorActivity()
    }

    private fun onProcessSingleEvent() {
        val start = System.currentTimeMillis()
        val event = PhoneEvent("5556", true, start, start + 10000, false,
                "SMS TEXT", null, null, STATE_PENDING, deviceName(), STATUS_ACCEPTED, isRead = false)
        startCallProcessingService(appContext, event)
        showToast(R.string.operation_complete)
    }

    private fun onStartProcessPendingEvents() {
        startPendingCallProcessorService(appContext)
        showToast(R.string.operation_complete)
    }

    private fun onClearLogs() {
        val dir = File(appContext.filesDir, "log")
        val logs = dir.listFiles()!!
        for (file in logs) {
            if (!file.delete()) {
                log.warn("Cannot delete file")
            }
        }
        showToast("Removed ${logs.size} log files")
    }

    private fun onAddHistoryItem() {
        database.putEvent(PhoneEvent("+79052345670", true, System.currentTimeMillis(), null, false,
                "Debug message", null, null, STATE_PENDING, deviceName(), STATUS_ACCEPTED, isRead = false))
        database.notifyChanged()
        showToast(R.string.operation_complete)
    }

    private fun onPopulateHistory() {
        var time = System.currentTimeMillis()
        val recipient = deviceName()
        database.putEvent(PhoneEvent("+79052345671", true, time, null, false, "Debug message", null, null, STATE_PENDING, recipient, STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("+79052345672", false, 1000.let { time += it; time }, null, false, "Debug message", null, null, STATE_PROCESSED, recipient, STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("+79052345673", true, 1000.let { time += it; time }, time + 10000, false, null, null, null, STATE_IGNORED, recipient, STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("+79052345674", false, 1000.let { time += it; time }, time + 10000, false, null, null, null, STATE_PENDING, recipient, STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("+79052345675", true, 1000.let { time += it; time }, time + 10000, true, null, null, null, STATE_PENDING, recipient, STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("+79052345671", true, 1000.let { time += it; time }, null, false, "Debug message", null, "Test exception +79052345671", STATE_PENDING, recipient, STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("+79052345672", false, 1000.let { time += it; time }, null, false, "Debug message", null, "Test exception +79052345672", STATE_PENDING, recipient, STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("+79052345673", true, 1000.let { time += it; time }, time + 10000, false, null, null, "Test exception +79052345673", STATE_PENDING, recipient, STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("+79052345674", false, 1000.let { time += it; time }, time + 10000, false, null, null, "Test exception +79052345674", STATE_PENDING, recipient, STATUS_ACCEPTED, isRead = false))
        database.putEvent(PhoneEvent("+79052345675", true, 1000.let { time += it; time }, time + 10000, true, null, null, "Test exception +79052345675", STATE_PENDING, recipient, STATUS_ACCEPTED, isRead = false))
        database.notifyChanged()
        showToast(R.string.operation_complete)
    }

    private fun onShowConcurrent() {
        val sb = StringBuilder()
        val intent = Intent("android.provider.Telephony.SMS_RECEIVED")
        val activities = appContext.packageManager.queryBroadcastReceivers(intent, 0)
        for (resolveInfo in activities) {
            val activityInfo = resolveInfo.activityInfo
            if (activityInfo != null) {
                sb.append(activityInfo.packageName)
                        .append(" : ")
                        .append(resolveInfo.priority)
                        .append("\n")
                log.debug("Concurrent package:" + activityInfo.packageName + " priority: " +
                        resolveInfo.priority)
            }
        }

        InfoDialog(message = sb.toString()).show(this)
    }

    private fun onGoogleDriveClear() {
        Tasks.call<Void>(Executors.newSingleThreadExecutor(), Callable {
            val drive = GoogleDrive(appContext)
            drive.login(primaryAccount(requireContext())!!)
            drive.clear()
            null
        })
        showToast(R.string.operation_complete)
    }

    private fun onGoogleDriveSync() {
        ConfirmDialog("Synchronize with drive?") {
            Tasks.call<Void>(Executors.newSingleThreadExecutor(), Callable {
                try {
                    Synchronizer(appContext, selectedAccount(requireContext())!!, database).sync()
                } catch (x: Throwable) {
                    log.error("Sync error: ", x)
                }
                null
            })
            showToast(R.string.operation_complete)
        }.show(this)
    }

    private fun onGoogleDriveDownload() {
        ConfirmDialog("Download from drive?") {
            Tasks.call<Void>(Executors.newSingleThreadExecutor(), Callable {
                try {
                    Synchronizer(appContext, selectedAccount(requireContext())!!, database).download()
                } catch (x: Throwable) {
                    log.error("Download error: ", x)
                }
                null
            })
            showToast(R.string.operation_complete)
        }.show(this)
    }

    private fun onGoogleDriveUpload() {
        ConfirmDialog("Upload to drive?") {
            Tasks.call<Void>(Executors.newSingleThreadExecutor(), Callable {
                try {
                    Synchronizer(appContext, selectedAccount(requireContext())!!, database).upload()
                    showToast(R.string.operation_complete)
                } catch (x: Throwable) {
                    log.error("Upload error: ", x)
                }
                null
            })
        }.show(this)
    }

    @SuppressLint("SetTextI18n")
    private fun onSendSms() {
        InputDialog(title = "Phone number",
                inputType = InputType.TYPE_CLASS_PHONE,
                initialValue = "5556",
                positiveAction = {
                    val sentIntent = PendingIntent.getBroadcast(appContext, 0, Intent("SMS_SENT"), 0)
                    val deliveredIntent = PendingIntent.getBroadcast(appContext, 0, Intent("SMS_DELIVERED"), 0)
                    try {
                        getDefault().sendTextMessage(it, null, "Debug message", sentIntent, deliveredIntent)
                    } catch (x: Throwable) {
                        log.error("Failed: ", x)
                        showToast("Failed")
                    }
                }
        )
    }

    private fun onShowAccounts() {
        val s = "Selected: ${selectedAccount(requireContext())?.name}\n\n" +
                "Service: ${serviceAccount(requireContext())?.name}\n\n" +
                "Primary: ${primaryAccount(requireContext())?.name}"
        InfoDialog(message = s).show(this)
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