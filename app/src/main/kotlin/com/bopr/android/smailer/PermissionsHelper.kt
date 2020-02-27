package com.bopr.android.smailer

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_CONTACT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_EMAIL_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_SMS
import com.bopr.android.smailer.ui.InfoDialog
import com.bopr.android.smailer.util.AndroidUtil.permissionLabel
import com.bopr.android.smailer.util.UiUtil.showToast
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Responsible for permissions checking.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PermissionsHelper(val activity: FragmentActivity) {

    private val log = LoggerFactory.getLogger("PermissionsHelper")
    private val requestResultCode = nextRequestResult++
    private val settings: Settings = Settings(activity)
    private var onComplete: (() -> Unit)? = null

    fun checkAll(onComplete: (() -> Unit)) {
        log.debug("Checking all")

        this.onComplete = onComplete
        checkPermissions(items.keys)
    }

    /**
     * To be added into owners's onRequestPermissionsResult()
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
                activity.showToast(R.string.since_permissions_not_granted)
            }

            onComplete()
        }
    }

    /**
     * To be added into owners's onSharedPreferenceChanged()
     */
    fun onSharedPreferenceChanged(key: String) {
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
                    requiredPermissions.add(PROCESS_OUTGOING_CALLS)
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
        }

        checkPermissions(requiredPermissions)
    }

    private fun onPermissionsGranted(permissions: MutableSet<String>) {
        log.debug("Granted: $permissions")
    }

    private fun onPermissionsDenied(permissions: Collection<String>) {
        log.debug("Denied: $permissions")

        if (permissions.isNotEmpty()) {
            val triggers = settings.getStringSet(PREF_EMAIL_TRIGGERS)
            val content = settings.getStringSet(PREF_EMAIL_CONTENT)

            if (permissions.contains(RECEIVE_SMS)) {
                triggers.remove(VAL_PREF_TRIGGER_IN_SMS)
            }
            if (permissions.contains(READ_SMS)) {
                triggers.remove(VAL_PREF_TRIGGER_OUT_SMS)
            }
            if (permissions.contains(READ_PHONE_STATE)) {
                triggers.remove(VAL_PREF_TRIGGER_IN_CALLS)
                triggers.remove(VAL_PREF_TRIGGER_MISSED_CALLS)
            }
            if (permissions.contains(PROCESS_OUTGOING_CALLS)) {
                triggers.remove(VAL_PREF_TRIGGER_OUT_CALLS)
            }
            if (permissions.contains(READ_CONTACTS)) {
                content.remove(VAL_PREF_EMAIL_CONTENT_CONTACT)
            }
            if (permissions.contains(ACCESS_COARSE_LOCATION) || permissions.contains(ACCESS_FINE_LOCATION)) {
                content.remove(VAL_PREF_EMAIL_CONTENT_LOCATION)
            }

            settings.edit()
                    .putStringSet(PREF_EMAIL_TRIGGERS, triggers)
                    .putStringSet(PREF_EMAIL_CONTENT, content)
                    .apply()
        }
    }

    private fun checkPermissions(permissions: Collection<String>) {
        if (permissions.isNotEmpty()) {
            log.debug("Checking: $permissions")

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
            } else {
                onComplete()
            }
        } else {
            onComplete()
        }
    }

    private fun requestPermissions(permissions: List<String>) {
        log.debug("Requesting : $permissions")

        ActivityCompat.requestPermissions(activity, permissions.toTypedArray(), requestResultCode)
    }

    private fun explainPermissions(permissions: List<String>) {
        log.debug("Explaining : $permissions")

        InfoDialog(message = formatRationale(permissions)) {
            requestPermissions(permissions)
        }.show(activity)
    }

    private fun formatRationale(permissions: Collection<String>): String {
        val sb = StringBuilder()
        for (permission in permissions) {
            sb.append(permissionRationale(activity, permission)).append("\n\n")
        }
        return sb.toString()
    }

    private fun onComplete() {
//        onComplete?.invoke().also { onComplete = null }
    }

    companion object {

        private val items: Map<String, Int> = sortedMapOf(
                RECEIVE_SMS to R.string.permission_rationale_receive_sms,
                SEND_SMS to R.string.permission_rationale_send_sms,
                READ_SMS to R.string.permission_rationale_read_sms,
                READ_PHONE_STATE to R.string.permission_rationale_phone_state,
                PROCESS_OUTGOING_CALLS to R.string.permission_rationale_outgoing_call,
                READ_CONTACTS to R.string.permission_rationale_read_contacts,
                ACCESS_COARSE_LOCATION to R.string.permission_rationale_coarse_location,
                ACCESS_FINE_LOCATION to R.string.permission_rationale_fine_location
        )

        fun permissionRationale(context: Context, permission: String): String {
            return context.getString(items.getValue(permission), permissionLabel(context, permission))
        }

        private var nextRequestResult = 200
    }

}