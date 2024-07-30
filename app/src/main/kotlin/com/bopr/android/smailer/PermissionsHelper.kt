package com.bopr.android.smailer

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_CALL_LOG
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.READ_SMS
import android.Manifest.permission.RECEIVE_SMS
import android.Manifest.permission.SEND_SMS
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.FragmentActivity
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSAGE_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_SENDER_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_TRIGGERS
import com.bopr.android.smailer.Settings.Companion.PREF_EMAIL_MESSENGER_RECIPIENTS
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_ACCOUNT
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_CALLER
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_MESSAGE_CONTENT_LOCATION
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_IN_SMS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_MISSED_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_CALLS
import com.bopr.android.smailer.Settings.Companion.VAL_PREF_TRIGGER_OUT_SMS
import com.bopr.android.smailer.ui.InfoDialog.Companion.showInfoDialog
import org.slf4j.LoggerFactory

/**
 * Responsible for permissions checking.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class PermissionsHelper(
    private val activity: FragmentActivity,
    private val onPermissionsRequestComplete: () -> Unit = {}
) {

    private val items: Map<String, Int> = sortedMapOf<String, Int>().apply {
        put(RECEIVE_SMS, R.string.permission_rationale_receive_sms)
        put(RECEIVE_SMS, R.string.permission_rationale_receive_sms)
        put(SEND_SMS, R.string.permission_rationale_send_sms)
        put(READ_SMS, R.string.permission_rationale_read_sms)
        put(READ_PHONE_STATE, R.string.permission_rationale_phone_state)
        put(READ_CALL_LOG, R.string.permission_rationale_phone_state)
        put(READ_CONTACTS, R.string.permission_rationale_read_contacts)
        put(ACCESS_COARSE_LOCATION, R.string.permission_rationale_coarse_location)
        put(ACCESS_FINE_LOCATION, R.string.permission_rationale_fine_location)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            put(POST_NOTIFICATIONS, R.string.permission_rationale_post_notifications)
        }
    }

    private val settings = Settings(activity)
    private val accountHelper = AccountHelper(activity)
    private val permissionRequestLauncher =
        activity.registerForActivityResult(RequestMultiplePermissions()) { result ->
            onPermissionRequestResult(result)
        }

    fun checkAll() {
        checkPermissions(items.keys)
    }

    private fun onPermissionRequestResult(result: Map<String, @JvmSuppressWildcards Boolean>) {
        val grantedPermissions = result.filterValues { it }.keys
        val deniedPermissions = result.filterValues { !it }.keys

        onPermissionsGranted(grantedPermissions)
        onPermissionsDenied(deniedPermissions)

        if (deniedPermissions.isNotEmpty()) {
            activity.showInfoDialog(messageRes = R.string.since_permissions_not_granted)
        } else {
            onPermissionsRequestComplete()
        }
    }

    /**
     * To be added into owner's onSharedPreferenceChanged()
     */
    internal fun onSettingsChanged(key: String?) {
        log.debug("Handling preference changed: $key")

        val requiredPermissions = mutableSetOf<String>()
        when (key) {
            PREF_EMAIL_TRIGGERS -> {
                val triggers = settings.getEmailTriggers()
                if (triggers.contains(VAL_PREF_TRIGGER_IN_SMS)) {
                    requiredPermissions.add(RECEIVE_SMS)
                }
                if (triggers.contains(VAL_PREF_TRIGGER_OUT_SMS)) {
                    requiredPermissions.add(READ_SMS)
                }
                if (triggers.contains(VAL_PREF_TRIGGER_IN_CALLS)
                    || triggers.contains(VAL_PREF_TRIGGER_OUT_CALLS)
                    || triggers.contains(VAL_PREF_TRIGGER_MISSED_CALLS)
                ) {
                    requiredPermissions.add(READ_PHONE_STATE)
                    requiredPermissions.add(READ_CALL_LOG)
                }
            }

            PREF_EMAIL_MESSAGE_CONTENT -> {
                val content = settings.getStringSet(PREF_EMAIL_MESSAGE_CONTENT)
                if (content.contains(VAL_PREF_MESSAGE_CONTENT_CALLER)) {
                    requiredPermissions.add(READ_CONTACTS)
                }
                if (content.contains(VAL_PREF_MESSAGE_CONTENT_LOCATION)) {
                    requiredPermissions.add(ACCESS_COARSE_LOCATION)
                    requiredPermissions.add(ACCESS_FINE_LOCATION)
                }
            }
        }

        checkPermissions(requiredPermissions)
    }

    private fun onPermissionsGranted(grantedPermissions: Set<String>) {
        log.debug("Granted: {}", grantedPermissions)

        /* set default accounts at startup */
        val accountName = accountHelper.getPrimaryGoogleAccount()?.name
        settings.update {
            ifNotExists(PREF_EMAIL_SENDER_ACCOUNT) { putString(it, accountName) }
            ifNotExists(PREF_EMAIL_MESSENGER_RECIPIENTS) { putString(it, accountName) }
            ifNotExists(PREF_REMOTE_CONTROL_ACCOUNT) { putString(it, accountName) }
        }
    }

    private fun onPermissionsDenied(deniedPermissions: Collection<String>) {
        log.debug("Denied: {}", deniedPermissions)

        if (deniedPermissions.isNotEmpty()) {
            val triggers = settings.getEmailTriggers()
            val content = settings.getStringSet(PREF_EMAIL_MESSAGE_CONTENT)

            if (deniedPermissions.contains(RECEIVE_SMS)) {
                triggers.remove(VAL_PREF_TRIGGER_IN_SMS)
            }
            if (deniedPermissions.contains(READ_SMS)) {
                triggers.remove(VAL_PREF_TRIGGER_OUT_SMS)
            }
            if (deniedPermissions.contains(READ_PHONE_STATE)
                || deniedPermissions.contains(READ_CALL_LOG)
            ) {
                triggers.remove(VAL_PREF_TRIGGER_IN_CALLS)
                triggers.remove(VAL_PREF_TRIGGER_OUT_CALLS)
                triggers.remove(VAL_PREF_TRIGGER_MISSED_CALLS)
            }
            if (deniedPermissions.contains(READ_CONTACTS)) {
                content.remove(VAL_PREF_MESSAGE_CONTENT_CALLER)
            }
            if (deniedPermissions.contains(ACCESS_COARSE_LOCATION)
                || deniedPermissions.contains(ACCESS_FINE_LOCATION)
            ) {
                content.remove(VAL_PREF_MESSAGE_CONTENT_LOCATION)
            }

            settings.update {
                putStringSet(PREF_EMAIL_TRIGGERS, triggers)
                putStringSet(PREF_EMAIL_MESSAGE_CONTENT, content)
            }
        }
    }

    private fun checkPermissions(permissions: Collection<String>) {
        if (permissions.isEmpty()) {
            onPermissionsRequestComplete()
        } else {
            log.debug("Checking: {}", permissions)

            val deniedPermissions = mutableListOf<String>()
            val unexplainedPermissions = mutableListOf<String>()

            for (p in permissions) {
                if (checkSelfPermission(activity, p) != PERMISSION_GRANTED) {
                    deniedPermissions.add(p)
                    if (shouldShowRequestPermissionRationale(activity, p)) {
                        unexplainedPermissions.add(p)
                    }
                }
            }

            if (deniedPermissions.isEmpty()) {
                onPermissionsRequestComplete()
            } else {
                if (unexplainedPermissions.isNotEmpty()) {
                    explainPermissions(unexplainedPermissions)
                } else {
                    requestPermissions(deniedPermissions)
                }
            }
        }
    }

    private fun requestPermissions(permissions: List<String>) {
        log.debug("Requesting : {}", permissions)
        permissionRequestLauncher.launch(permissions.toTypedArray())
    }

    private fun explainPermissions(permissions: List<String>) {
        log.debug("Explaining : {}", permissions)

        activity.showInfoDialog(message = formatRationale(permissions)) {
            requestPermissions(permissions)
        }
    }

    private fun formatRationale(permissions: Collection<String>): String {
        return buildString {
            for (permission in permissions) {
                val value = items.getValue(permission)
                append(activity.getString(value, getLabelFor(permission)))
                append("\n\n")
            }
        }
    }

    private fun getLabelFor(permissionName: String): String {
        return activity.packageManager.run {
            getPermissionInfo(permissionName, 0).loadLabel(this).toString()
        }
    }

    companion object {


        private val log = LoggerFactory.getLogger("PermissionsHelper")
    }

}