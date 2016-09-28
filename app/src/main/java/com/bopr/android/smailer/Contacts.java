package com.bopr.android.smailer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.Manifest.permission.READ_CONTACTS;
import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.PhoneLookup;

/**
 * Contacts utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Contacts {

    private static Logger log = LoggerFactory.getLogger("Contacts");

    @Nullable
    public static String getContactName(Context context, String phoneNumber) {
        String result = null;
        if (!permissionDenied(context)) {
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = context.getContentResolver().query(uri,
                    new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
                }
                cursor.close();
            }
        }
        return result;
    }

    @Nullable
    public static String getEmailAddress(Context context, String emailId) {
        String result = null;
        if (!permissionDenied(context)) {
            Cursor cursor = context.getContentResolver().query(Email.CONTENT_URI, null,
                    Email._ID + "=" + emailId, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(Email.DATA));
                }
                cursor.close();
            }
        }
        return result;
    }

    public static boolean isPermissionsDenied(Context context) {
        return PermissionsChecker.isPermissionsDenied(context, READ_CONTACTS);
    }

    private static boolean permissionDenied(Context context) {
        if (isPermissionsDenied(context)) {
            log.warn("Unable read contact. Permission denied.");
            return true;
        }
        return false;
    }

    public static Intent createPickContactEmailIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(Email.CONTENT_TYPE);
        return intent;
    }

    public static String getEmailAddressFromIntent(Context context, Intent intent) {
        return getEmailAddress(context, intent.getData().getLastPathSegment());
    }
}
