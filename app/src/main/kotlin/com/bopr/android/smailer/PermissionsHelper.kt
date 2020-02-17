package com.bopr.android.smailer

import android.Manifest.permission.*
import android.app.Activity
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.bopr.android.smailer.util.TagFormatter
import com.bopr.android.smailer.util.Util.requireNonNull
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

    init {
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    fun dispose() {
        settings.unregisterOnSharedPreferenceChangeListener(this)

        log.debug("Disposed")
    }

    fun checkAll() {
        log.debug("Checking all")

        doCheck(items.keys)
    }

    /**
     * To be added into activity's onRequestPermissionsResult()
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == requestResultCode) {
            val deniedPermissions: MutableSet<String> = HashSet()
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }
            if (deniedPermissions.isNotEmpty()) {
                onPermissionsDenied(deniedPermissions)
                showDenialImpact()
            }
        }
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, preference: String) {
        val deniedPermissions: MutableSet<String> = HashSet()
        when (preference) {
            PREF_EMAIL_TRIGGERS -> {
                val triggers = preferences.getStringSet(PREF_EMAIL_TRIGGERS, emptySet())
                if (triggers!!.contains(VAL_PREF_TRIGGER_IN_SMS)) {
                    deniedPermissions.add(RECEIVE_SMS)
                }
                if (triggers.contains(VAL_PREF_TRIGGER_OUT_SMS)) {
                    deniedPermissions.add(READ_SMS)
                }
                if (triggers.contains(VAL_PREF_TRIGGER_IN_CALLS) || preferences.contains(VAL_PREF_TRIGGER_MISSED_CALLS)) {
                    deniedPermissions.add(READ_PHONE_STATE)
                }
                if (triggers.contains(VAL_PREF_TRIGGER_OUT_CALLS)) {
                    @Suppress("DEPRECATION")
                    deniedPermissions.add(PROCESS_OUTGOING_CALLS) // TODO: 06.02.2020 deprecated
                }
            }
            PREF_EMAIL_CONTENT -> {
                val content = preferences.getStringSet(PREF_EMAIL_CONTENT, emptySet())
                if (content!!.contains(VAL_PREF_EMAIL_CONTENT_CONTACT)) {
                    deniedPermissions.add(READ_CONTACTS)
                }
                if (content.contains(VAL_PREF_EMAIL_CONTENT_LOCATION)) {
                    deniedPermissions.add(ACCESS_COARSE_LOCATION)
                    deniedPermissions.add(ACCESS_FINE_LOCATION)
                }
            }
            PREF_MARK_SMS_AS_READ -> deniedPermissions.add(WRITE_SMS)
        }
        doCheck(deniedPermissions)
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
                        edit.removeFromStringSet(PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_IN_CALLS, VAL_PREF_TRIGGER_MISSED_CALLS)
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
        if (permissions.isNotEmpty()) {
            val deniedPermissions: MutableList<String> = ArrayList()
            val unexplainedPermissions: MutableList<String> = ArrayList()

            for (p in permissions) {
                if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(p)
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p)) {
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

        AlertDialog.Builder(activity)
                .setMessage(formatRationale(permissions))
                .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissions(permissions) }
                .show()
    }

    private fun formatRationale(permissions: Collection<String>): String {
        val b = StringBuilder()
        for (permission in permissions) {
            val line = TagFormatter(activity)
                    .pattern(requireNonNull(items[permission]))
                    .put("permission", permissionLabel(permission))
                    .format()
            b.append(line).append("\n\n")
        }
        return b.toString()
    }

    private fun permissionLabel(permission: String): String {
        return try {
            val packageManager = activity.packageManager
            val info = packageManager.getPermissionInfo(permission, 0)
            info.loadLabel(packageManager).toString()
        } catch (x: PackageManager.NameNotFoundException) {
            throw RuntimeException(x)
        }
    }

    private fun showDenialImpact() {
        AlertDialog.Builder(activity)
                .setMessage(R.string.since_permissions_not_granted)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    companion object {

        private const val WRITE_SMS = "android.permission.WRITE_SMS"
        private var nextRequestResult = 200
    }

}