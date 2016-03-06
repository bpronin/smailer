package com.bopr.android.smailer.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;

import static android.os.Build.MANUFACTURER;
import static android.os.Build.MODEL;

/**
 * Class DeviceUtil.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class ContactUtil {

    public static String getContactName(Context context, String phoneNumber) {
        String result = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                result = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

}
