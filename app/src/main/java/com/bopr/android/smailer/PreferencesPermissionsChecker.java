package com.bopr.android.smailer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;

import com.bopr.android.smailer.util.TagFormatter;
import com.bopr.android.smailer.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.PROCESS_OUTGOING_CALLS;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_MARK_SMS_AS_READ;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_CONTACT;
import static com.bopr.android.smailer.Settings.VAL_PREF_EMAIL_CONTENT_LOCATION;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static com.bopr.android.smailer.util.AndroidUtil.isPermissionsDenied;
import static com.bopr.android.smailer.util.ResourceUtil.showToast;
import static com.bopr.android.smailer.util.TagFormatter.formatter;
import static com.bopr.android.smailer.util.Util.requireNonNull;
import static java.util.Arrays.asList;

/**
 * Responsible for permissions checking.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class PreferencesPermissionsChecker implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String WRITE_SMS = "android.permission.WRITE_SMS";

    private static AtomicInteger nextRequestResult = new AtomicInteger(200);

    private Activity activity;
    private Settings settings;
    private int requestResultCode = nextRequestResult.incrementAndGet();
    private Map<String, Integer> rationales = new HashMap<>();

    protected PreferencesPermissionsChecker(Activity activity, Settings settings) {
        this.activity = activity;
        this.settings = settings;
        this.settings.registerOnSharedPreferenceChangeListener(this);

        rationales.put(RECEIVE_SMS, R.string.permission_rationale_receive_sms);
        rationales.put(WRITE_SMS, R.string.permission_rationale_write_sms);
        rationales.put(READ_SMS, R.string.permission_rationale_read_sms);
        rationales.put(READ_PHONE_STATE, R.string.permission_rationale_phone_state);
        rationales.put(PROCESS_OUTGOING_CALLS, R.string.permission_rationale_outgoing_call);
        rationales.put(READ_CONTACTS, R.string.permission_rationale_read_contacts);
        rationales.put(ACCESS_COARSE_LOCATION, R.string.permission_rationale_coarse_location);
        rationales.put(ACCESS_FINE_LOCATION, R.string.permission_rationale_fine_location);
    }

    public void destroy() {
        settings.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void checkAll() {
        boolean neverRequested = false;

        for (String permission : rationales.keySet()) {
            if (isPermissionsDenied(activity, permission) && !needExplanation(permission)) {
                neverRequested = true;
                break;
            }
        }

        if (neverRequested) {
            check(rationales.keySet());
        }
    }

    public void handleRequestResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == requestResultCode) {
            Set<String> deniedPermissions = new HashSet<>();

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }

            if (!deniedPermissions.isEmpty()) {
                showToast(activity, R.string.since_permissions_not_granted);
                onPermissionsDenied(deniedPermissions);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        Set<String> requiredPermissions = new HashSet<>();

        switch (key) {
            case KEY_PREF_EMAIL_TRIGGERS:
                Set<String> triggers = preferences.getStringSet(KEY_PREF_EMAIL_TRIGGERS, Collections.<String>emptySet());
                if (triggers.contains(VAL_PREF_TRIGGER_IN_SMS)) {
                    requiredPermissions.add(RECEIVE_SMS);
                }
                if (triggers.contains(VAL_PREF_TRIGGER_OUT_SMS)) {
                    requiredPermissions.add(READ_SMS);
                }
                if (triggers.contains(VAL_PREF_TRIGGER_IN_CALLS) || preferences.contains(VAL_PREF_TRIGGER_MISSED_CALLS)) {
                    requiredPermissions.add(READ_PHONE_STATE);
                }
                if (triggers.contains(VAL_PREF_TRIGGER_OUT_CALLS)) {
                    requiredPermissions.add(PROCESS_OUTGOING_CALLS);
                }
                break;
            case KEY_PREF_EMAIL_CONTENT:
                Set<String> content = preferences.getStringSet(KEY_PREF_EMAIL_CONTENT, Collections.<String>emptySet());
                if (content.contains(VAL_PREF_EMAIL_CONTENT_CONTACT)) {
                    requiredPermissions.add(READ_CONTACTS);
                }
                if (content.contains(VAL_PREF_EMAIL_CONTENT_LOCATION)) {
                    requiredPermissions.add(ACCESS_COARSE_LOCATION);
                    requiredPermissions.add(ACCESS_FINE_LOCATION);
                }
                break;
            case KEY_PREF_MARK_SMS_AS_READ:
                requiredPermissions.add(WRITE_SMS);
                break;
        }

        check(requiredPermissions);
    }

    protected void onPermissionsDenied(Collection<String> permissions) {
        for (String permission : permissions) {
            switch (permission) {
                case RECEIVE_SMS:
                    removeSetPreferenceValue(settings, KEY_PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_IN_SMS);
                    break;
                case READ_SMS:
                    removeSetPreferenceValue(settings, KEY_PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_OUT_SMS);
                    break;
                case WRITE_SMS:
                    removeSetPreferenceValue(settings, KEY_PREF_MARK_SMS_AS_READ);
                    break;
                case READ_PHONE_STATE:
                    removeSetPreferenceValue(settings, KEY_PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_IN_CALLS, VAL_PREF_TRIGGER_MISSED_CALLS);
                    break;
                case PROCESS_OUTGOING_CALLS:
                    removeSetPreferenceValue(settings, KEY_PREF_EMAIL_TRIGGERS, VAL_PREF_TRIGGER_OUT_CALLS);
                    break;
                case READ_CONTACTS:
                    removeSetPreferenceValue(settings, KEY_PREF_EMAIL_CONTENT, VAL_PREF_EMAIL_CONTENT_CONTACT);
                    break;
                case ACCESS_COARSE_LOCATION:
                case ACCESS_FINE_LOCATION:
                    removeSetPreferenceValue(settings, KEY_PREF_EMAIL_CONTENT, VAL_PREF_EMAIL_CONTENT_LOCATION);
                    break;
            }
        }
    }

    private void check(Collection<String> permissions) {
        if (!permissions.isEmpty()) {
            List<String> requiredPermissions = new ArrayList<>();
            List<String> explainedPermissions = new ArrayList<>();

            for (String permission : permissions) {
                if (isPermissionsDenied(activity, permission)) {
                    requiredPermissions.add(permission);
                    if (needExplanation(permission)) {
                        explainedPermissions.add(permission);
                    }
                }
            }

            request(requiredPermissions, explainedPermissions);
        }
    }

    private void request(final List<String> requiredPermissions,
                         final List<String> explainedPermissions) {
        if (!requiredPermissions.isEmpty()) {
            if (!explainedPermissions.isEmpty()) {
                new AlertDialog.Builder(activity)
                        .setMessage(formatRationale(explainedPermissions))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                request(requiredPermissions);
                            }
                        })
                        .show();
            } else {
                request(requiredPermissions);
            }
        }
    }

    private boolean needExplanation(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    private void request(Collection<String> permissions) {
        ActivityCompat.requestPermissions(activity, Util.toArray(permissions), requestResultCode);
    }

    private String formatRationale(Collection<String> permissions) {
        StringBuilder b = new StringBuilder();
        for (String permission : permissions) {
            TagFormatter line = formatter(activity)
                    .pattern(requireNonNull(rationales.get(permission)))
                    .put("permission", getPermissionLabel(permission));
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
            throw new Error(x);
        }
    }

    private void removeSetPreferenceValue(Settings settings, String key, String... values) {
        Set<String> set = settings.getStringSet(key, Collections.<String>emptySet());
        set.removeAll(asList(values));
        settings.edit().putStringSet(key, set).apply();
    }

}
