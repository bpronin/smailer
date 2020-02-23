package com.bopr.android.smailer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.bopr.android.smailer.util.SharedPreferencesWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.PROCESS_OUTGOING_CALLS;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.PREF_MARK_SMS_AS_READ;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_CONTACT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static com.bopr.android.smailer.util.AndroidUtil.checkPermission;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.google.common.collect.Iterables.toArray;

/**
 * Responsible for permissions checking.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class PermissionsHelper implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static Logger log = LoggerFactory.getLogger("PermissionsHelper");

    private static final String WRITE_SMS = "android.permission.WRITE_SMS";

    private static int nextRequestResult = 200;

    private int requestResultCode = nextRequestResult++;
    private Activity activity;
    private Settings settings;
    private Map<String, Integer> items = new HashMap<>();

    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // TODO: 06.02.2020 deprecated
    protected PermissionsHelper(Activity activity, Settings settings) {
        this.activity = activity;
        this.settings = settings;
        this.settings.registerChangeListener(this);

        items.put(RECEIVE_SMS, R.string.permission_rationale_receive_sms);
        items.put(WRITE_SMS, R.string.permission_rationale_write_sms);
        items.put(READ_SMS, R.string.permission_rationale_read_sms);
        items.put(READ_PHONE_STATE, R.string.permission_rationale_phone_state);
        items.put(PROCESS_OUTGOING_CALLS, R.string.permission_rationale_outgoing_call);
        items.put(READ_CONTACTS, R.string.permission_rationale_read_contacts);
        items.put(ACCESS_COARSE_LOCATION, R.string.permission_rationale_coarse_location);
        items.put(ACCESS_FINE_LOCATION, R.string.permission_rationale_fine_location);
        items.put(SEND_SMS, R.string.permission_rationale_send_sms);
    }

    public void dispose() {
        settings.unregisterChangeListener(this);
        log.debug("Disposed");
    }

    public void checkAll() {
        log.debug("Checking all");

        doCheck(items.keySet());
    }

    /**
     * To be added into activity's onRequestPermissionsResult()
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == requestResultCode) {
            Set<String> deniedPermissions = new HashSet<>();

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }

            if (!deniedPermissions.isEmpty()) {
                onPermissionsDenied(deniedPermissions);
                showDenialImpact();
            }
        }
    }

    @Override
    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // TODO: 06.02.2020 deprecated
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Set<String> deniedPermissions = new HashSet<>();

        switch (key) {
            case PREF_EMAIL_TRIGGERS:
                Set<String> triggers = settings.getStringSet(PREF_EMAIL_TRIGGERS);
                if (triggers.contains(VAL_PREF_TRIGGER_IN_SMS)) {
                    deniedPermissions.add(RECEIVE_SMS);
                }
                if (triggers.contains(VAL_PREF_TRIGGER_OUT_SMS)) {
                    deniedPermissions.add(READ_SMS);
                }
                if (triggers.contains(VAL_PREF_TRIGGER_IN_CALLS) ||
                        triggers.contains(VAL_PREF_TRIGGER_MISSED_CALLS)) {
                    deniedPermissions.add(READ_PHONE_STATE);
                }
                if (triggers.contains(VAL_PREF_TRIGGER_OUT_CALLS)) {
                    deniedPermissions.add(PROCESS_OUTGOING_CALLS);
                }
                break;
            case PREF_EMAIL_CONTENT:
                Set<String> content = settings.getStringSet(PREF_EMAIL_CONTENT);
                if (content.contains(VAL_PREF_EMAIL_CONTENT_CONTACT)) {
                    deniedPermissions.add(READ_CONTACTS);
                }
                if (content.contains(VAL_PREF_EMAIL_CONTENT_LOCATION)) {
                    deniedPermissions.add(ACCESS_COARSE_LOCATION);
                    deniedPermissions.add(ACCESS_FINE_LOCATION);
                }
                break;
            case PREF_MARK_SMS_AS_READ:
                deniedPermissions.add(WRITE_SMS);
                break;
        }

        doCheck(deniedPermissions);
    }

    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // TODO: 06.02.2020 deprecated
    protected void onPermissionsDenied(Collection<String> permissions) {
        log.debug("Denied: " + permissions);

        if (!permissions.isEmpty()) {
            SharedPreferencesWrapper.EditorWrapper edit = settings.edit();

            for (String permission : permissions) {
                switch (permission) {
                    case RECEIVE_SMS:
                        edit.removeFromStringSet(PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_IN_SMS);
                        break;
                    case READ_SMS:
                        edit.removeFromStringSet(PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_OUT_SMS);
                        break;
                    case WRITE_SMS:
                        edit.removeFromStringSet(PREF_MARK_SMS_AS_READ);
                        break;
                    case READ_PHONE_STATE:
                        edit.removeFromStringSet(PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_IN_CALLS, VAL_PREF_TRIGGER_MISSED_CALLS);
                        break;
                    case PROCESS_OUTGOING_CALLS:
                        edit.removeFromStringSet(PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_OUT_CALLS);
                        break;
                    case READ_CONTACTS:
                        edit.removeFromStringSet(PREF_EMAIL_CONTENT, VAL_PREF_EMAIL_CONTENT_CONTACT);
                        break;
                    case ACCESS_COARSE_LOCATION:
                    case ACCESS_FINE_LOCATION:
                        edit.removeFromStringSet(PREF_EMAIL_CONTENT, VAL_PREF_EMAIL_CONTENT_LOCATION);
                        break;
                }
            }

            edit.apply();
        }
    }

    private void doCheck(Collection<String> permissions) {
        if (!permissions.isEmpty()) {
            List<String> deniedPermissions = new ArrayList<>();
            List<String> unexplainedPermissions = new ArrayList<>();

            for (String permission : permissions) {
                if (checkSelfPermission(activity, permission) != PERMISSION_GRANTED) {
                    deniedPermissions.add(permission);
                    if (shouldShowRequestPermissionRationale(activity, permission)) {
                        unexplainedPermissions.add(permission);
                    }
                }
            }

            if (!deniedPermissions.isEmpty()) {
                if (!unexplainedPermissions.isEmpty()) {
                    explainPermissions(unexplainedPermissions);
                } else {
                    requestPermissions(deniedPermissions);
                }
            }
        }
    }

    private void requestPermissions(List<String> permissions) {
        log.debug("Requesting : " + permissions);

        ActivityCompat.requestPermissions(activity, toArray(permissions, String.class), requestResultCode);
    }

    private void explainPermissions(final List<String> permissions) {
        log.debug("Explaining : " + permissions);

        new AlertDialog.Builder(activity)
                .setMessage(formatRationale(permissions))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> requestPermissions(permissions))
                .show();
    }

    private String formatRationale(Collection<String> permissions) {
        StringBuilder b = new StringBuilder();
        for (String permission : permissions) {
            String line = formatter(activity)
                    .pattern(Objects.requireNonNull(items.get(permission)))
                    .put("permission", getPermissionLabel(permission))
                    .format();
            b.append(line).append("\n\n");
        }
        return b.toString();
    }

    private String getPermissionLabel(String permission) {
        try {
            PackageManager packageManager = activity.getPackageManager();
            PermissionInfo info = packageManager.getPermissionInfo(permission, 0);
            return info.loadLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException x) {
            throw new RuntimeException(x);
        }
    }

    private void showDenialImpact() {
        new AlertDialog.Builder(activity)
                .setMessage(R.string.since_permissions_not_granted)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    static boolean isLocationPermissionsGranted(Context context) {
        return checkPermission(context, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION);
    }

}
