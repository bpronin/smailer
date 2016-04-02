package com.bopr.android.smailer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.bopr.android.smailer.util.AndroidUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Class PermissionsChecker.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class PermissionsChecker<V> {

    private static volatile int nextRequestResult = 200;

    private Activity activity;
    private String[] permissions;
    private int rationale;
    private int denyMessage;
    private int requestResultCode = nextRequestResult++;

    public PermissionsChecker(Activity activity) {
        this.activity = activity;
    }

    public void setPermissions(String... permissions) {
        this.permissions = permissions;
    }

    public void setDenyMessage(int denyMessageResourceId) {
        this.denyMessage = denyMessageResourceId;
    }

    public void setRationaleMessage(int rationaleResourceId) {
        this.rationale = rationaleResourceId;
    }

    public void check(V value) {
        if (isPermissionRequired(value) && isPermissionsDenied(activity, permissions)) {
            if (needExplanation()) {
                AndroidUtil.dialogBuilder(activity)
                        .setMessage(rationale)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions();
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                onPermissionsDenied();
                            }
                        })
                        .show();
            } else {
                requestPermissions();
            }
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(activity, permissions, requestResultCode);
    }

    public void onPermissionsRequestResult(int requestCode, int[] grantResults) {
        if (requestCode == this.requestResultCode) {
            if (grantResults[0] != PERMISSION_GRANTED) {
                Toast.makeText(activity, denyMessage, Toast.LENGTH_LONG).show();
                onPermissionsDenied();
            }
        }
    }

    private boolean needExplanation() {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    protected abstract boolean isPermissionRequired(V value);

    protected abstract void onPermissionsDenied();

    public static boolean isPermissionsDenied(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

}
