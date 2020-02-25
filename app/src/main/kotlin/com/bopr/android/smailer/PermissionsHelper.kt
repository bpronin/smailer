package com.bopr.android.smailer

import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_MARK_SMS_AS_READ
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_CONTACT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_SMS
import com.bopr.android.smailer.sync.SyncEngine.startSyncEngine
import com.bopr.android.smailer.util.AndroidUtil.permissionLabel
import com.bopr.android.smailer.util.Dialogs.showMessageDialog
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Responsible for permissions checking.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PermissionsHelper(private val activity: Activity) : OnSharedPreferenceChangeListener {

    private val log = LoggerFactory.getLogger("PermissionsHelper")
    private val requestResultCode = nextRequestResult++
    private val settings: Settings = Settings(activity)

    init {
        settings.registerChangeListener(this)
    }

    fun dispose() {
        settings.unregisterChangeListener(this)

        log.debug("Disposed")
    }

    fun checkAll(onDone: () -> Unit) {
        log.debug("Checking all")

        doCheck(items.keys)
    }

    /**
     * To be added into activity's onRequestPermissionsResult()
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == requestResultCode) {
            val grantedPermissions: MutableSet<String> = HashSet()
            val deniedPermissions: MutableSet<String> = HashSet()
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permissions[i])
                } else {
                    deniedPermissions.add(permissions[i])
                }
            }

            onPermissionsGranted(grantedPermissions)
            onPermissionsDenied(deniedPermissions)

            if (deniedPermissions.isNotEmpty()) {
                showMessageDialog(activity, activity.getString(R.string.since_permissions_not_granted))
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        log.debug("Handling preference changed: $key")

        val requiredPermissions: MutableSet<String> = HashSet()
        when (key) {
            PREF_EMAIL_TRIGGERS -> {
                val triggers = settings.getStringSet(PREF_EMAIL_TRIGGERS)
                if (triggers.contains(VAL_PREF_TRIGGER_IN_SMS)) {
                    requiredPermissions.add(RECEIVE_SMS)
                }
                if (triggers.contains(VAL_PREF_TRIGGER_OUT_SMS)) {
                    requiredPermissions.add(READ_SMS)
                }
                if (triggers.contains(VAL_PREF_TRIGGER_IN_CALLS) ||
                        triggers.contains(VAL_PREF_TRIGGER_MISSED_CALLS)) {
                    requiredPermissions.add(READ_PHONE_STATE)
                }
                if (triggers.contains(VAL_PREF_TRIGGER_OUT_CALLS)) {
                    @Suppress("DEPRECATION")
                    requiredPermissions.add(PROCESS_OUTGOING_CALLS) // TODO: 06.02.2020 deprecated
                }
            }
            PREF_EMAIL_CONTENT -> {
                val content = settings.getStringSet(PREF_EMAIL_CONTENT)
                if (content.contains(VAL_PREF_EMAIL_CONTENT_CONTACT)) {
                    requiredPermissions.add(READ_CONTACTS)
                }
                if (content.contains(VAL_PREF_EMAIL_CONTENT_LOCATION)) {
                    requiredPermissions.add(ACCESS_COARSE_LOCATION)
                    requiredPermissions.add(ACCESS_FINE_LOCATION)
                }
            }
            PREF_MARK_SMS_AS_READ -> requiredPermissions.add(WRITE_SMS)
        }
        doCheck(requiredPermissions)
    }

    private fun onPermissionsGranted(permissions: MutableSet<String>) {
        log.debug("Denied: $permissions")

        if (permissions.isNotEmpty()) {
            for (p in permissions) {
                when (p) {
                    GET_ACCOUNTS ->
                        startSyncEngine(activity)
                }
            }
        }
    }

    private fun onPermissionsDenied(permissions: Collection<String>) {
        log.debug("Denied: $permissions")

        if (permissions.isNotEmpty()) {
            val edit = settings.edit()

            for (p in permissions) {
                @Suppress("DEPRECATION")
                when (p) {
                    RECEIVE_SMS ->
                        edit.removeFromStringSet(PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_IN_SMS)
                    READ_SMS ->
                        edit.removeFromStringSet(PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_OUT_SMS)
                    WRITE_SMS ->
                        edit.removeFromStringSet(PREF_MARK_SMS_AS_READ)
                    READ_PHONE_STATE ->
                        edit.removeFromStringSet(PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_IN_CALLS,
                                VAL_PREF_TRIGGER_MISSED_CALLS)
                    PROCESS_OUTGOING_CALLS ->
                        edit.removeFromStringSet(PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_OUT_CALLS)
                    READ_CONTACTS ->
                        edit.removeFromStringSet(PREF_EMAIL_CONTENT, VAL_PREF_EMAIL_CONTENT_CONTACT)
                    ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION ->
                        edit.removeFromStringSet(PREF_EMAIL_CONTENT, VAL_PREF_EMAIL_CONTENT_LOCATION)
                }
            }

            edit.apply()
        }
    }

    private fun doCheck(permissions: Collection<String>) {
        log.debug("Checking: $permissions")

        if (permissions.isNotEmpty()) {
            val deniedPermissions: MutableList<String> = ArrayList()
            val unexplainedPermissions: MutableList<String> = ArrayList()

            for (p in permissions) {
                if (checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(p)
                    if (shouldShowRequestPermissionRationale(activity, p)) {
                        unexplainedPermissions.add(p)
                    }
                }
            }

            if (deniedPermissions.isNotEmpty()) {
                if (unexplainedPermissions.isNotEmpty()) {
                    explainPermissions(unexplainedPermissions)
                } else {
                    requestPermissions(deniedPermissions)
                }
            }
        }
    }

    private fun requestPermissions(permissions: List<String>) {
        log.debug("Requesting : $permissions")

        ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), requestResultCode)
    }

    private fun explainPermissions(permissions: List<String>) {
        log.debug("Explaining : $permissions")
        showMessageDialog(activity, formatRationale(permissions)) {
            requestPermissions(permissions)
        }
    }

    private fun formatRationale(permissions: Collection<String>): String {
        val sb = StringBuilder()
        for (permission in permissions) {
            sb.append(permissionRationale(activity, permission)).append("\n\n")
        }
        return sb.toString()
    }

    companion object {

        const val WRITE_SMS = "android.permission.WRITE_SMS"

        @Suppress("DEPRECATION")
        private val items: Map<String, Int> = mapOf(
                RECEIVE_SMS to R.string.permission_rationale_receive_sms,
                WRITE_SMS to R.string.permission_rationale_write_sms,
                READ_SMS to R.string.permission_rationale_read_sms,
                READ_PHONE_STATE to R.string.permission_rationale_phone_state,
                PROCESS_OUTGOING_CALLS to R.string.permission_rationale_outgoing_call, // TODO: 06.02.2020 deprecated
                READ_CONTACTS to R.string.permission_rationale_read_contacts,
                ACCESS_COARSE_LOCATION to R.string.permission_rationale_coarse_location,
                ACCESS_FINE_LOCATION to R.string.permission_rationale_fine_location,
                SEND_SMS to R.string.permission_rationale_send_sms
        )

        fun permissionRationale(context: Context, permission: String): String {
            return context.getString(items.getValue(permission), permissionLabel(context, permission))
        }

        private var nextRequestResult = 200
    }

}