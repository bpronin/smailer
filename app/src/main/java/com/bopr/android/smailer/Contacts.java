package com.bopr.android.smailer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.Manifest.permission.READ_CONTACTS;
import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.PhoneLookup;

/**
 * Operations with contacts.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Contacts {

    private static final String TAG = "Contacts";

    private static boolean permissionDenied(Context context) {
        if (PermissionsChecker.isPermissionsDenied(context, READ_CONTACTS)) {
            Log.w(TAG, "Unable read contact. Permission denied.");
            return true;
        }
        return false;
    }

    @Nullable
    public static String getContactName(Context context, String phoneNumber) {
        if (permissionDenied(context)) {
            return null;
        }

        String result = null;
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }
        return result;
    }

    @Nullable
    public static String getEmailAddress(Context context, String emailId) {
        if (permissionDenied(context)) {
            return null;
        }

        String result = null;
        Cursor cursor = context.getContentResolver().query(Email.CONTENT_URI, null,
                Email._ID + "=" + emailId, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(Email.DATA));
            }
            cursor.close();
        }
        return result;
    }

    public static Intent createPickContactEmailIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(Email.CONTENT_TYPE);
        return intent;
    }

    public static String getEmailAddressFromIntent(Context context, Intent intent) {
        return getEmailAddress(context, intent.getData().getLastPathSegment());
    }
}
