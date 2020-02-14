package com.bopr.android.smailer.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bopr.android.smailer.PhoneEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.Manifest.permission.READ_CONTACTS;
import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.PhoneLookup;
import static com.bopr.android.smailer.util.Util.requireNonNull;

/**
 * Content utilities.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public abstract class ContentUtils {

    private static Logger log = LoggerFactory.getLogger("ContentUtils");

    private ContentUtils() {
    }

    @Nullable
    public static String getContactName(@NonNull Context context, @NonNull String phone) {
        String result = null;
        if (requireReadContactPermission(context) && !TextUtil.isNullOrEmpty(phone)) {
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
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
    private static String getEmailAddress(@NonNull Context context, @NonNull String emailId) {
        String result = null;
        if (requireReadContactPermission(context)) {
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

    @Nullable
    private static String getPhone(@NonNull Context context, @NonNull String phoneId) {
        String result = null;
        if (requireReadContactPermission(context)) {
            Cursor cursor = context.getContentResolver().query(Phone.CONTENT_URI, null,
                    Phone._ID + "=" + phoneId, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(Phone.DATA));
                }
                cursor.close();
            }
        }
        return result;
    }

    public static Intent createPickContactEmailIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(Email.CONTENT_TYPE);
        return intent;
    }

    public static String getEmailAddressFromIntent(@NonNull Context context, @NonNull Intent intent) {
        return getEmailAddress(context, requireNonNull(intent.getData()).getLastPathSegment());
    }

    public static String getPhoneFromIntent(@NonNull Context context, @NonNull Intent intent) {
        return getPhone(context, requireNonNull(intent.getData()).getLastPathSegment());
    }

    public static boolean isReadContactsPermissionsDenied(@NonNull Context context) {
        return AndroidUtil.isPermissionsDenied(context, READ_CONTACTS);
    }

    private static boolean requireReadContactPermission(@NonNull Context context) {
        if (isReadContactsPermissionsDenied(context)) {
            log.warn("Unable read contact. Permission denied.");
            return false;
        }
        return true;
    }

    public static void markSmsAsRead(@NonNull Context context, @NonNull PhoneEvent event) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.parse("content://sms/inbox");

        Cursor cursor = contentResolver.query(uri, null, "read = 0 AND address = ? AND date_sent = ?",
                new String[]{event.getPhone(), String.valueOf(event.getStartTime())}, null);
        if (cursor == null) {
            throw new NullPointerException("Cannot obtain cursor");
        }

        try {
            if (cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndex("_id"));

                ContentValues values = new ContentValues();
                values.put("read", true);
                values.put("seen", true);
                contentResolver.update(uri, values, "_id=" + id, null);

                log.debug("SMS marked as read. " + event);
            }
        } catch (Exception e) {
            log.error("Mark SMS as read failed. ", e);
        } finally {
            cursor.close();
        }
    }
}
