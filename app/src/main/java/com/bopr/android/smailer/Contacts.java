package com.bopr.android.smailer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import static android.provider.ContactsContract.PhoneLookup;

/**
 * Operations with contacts.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class Contacts {

//    private static final String TAG = "Contacts";

    public static String getContactName(Context context, String phoneNumber) {
//        if (Permissions.isReadContactPermissionDenied(context)) {
//            Log.w(TAG, "Unable read contact. Permission denied.");
//            return null;
//        }

        String result = null;
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

}
