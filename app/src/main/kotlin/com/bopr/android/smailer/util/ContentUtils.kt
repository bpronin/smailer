package com.bopr.android.smailer.util

import android.Manifest.permission
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.Uri.encode
import android.net.Uri.withAppendedPath
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI
import android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.util.AndroidUtil.isPermissionsDenied
import org.slf4j.LoggerFactory

/**
 * Content utilities.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
object ContentUtils {

    private val log = LoggerFactory.getLogger("ContentUtils")
    
    fun contactName(context: Context, phone: String): String? {
        var result: String? = null
        if (requireReadContactPermission(context) && phone.isNotEmpty()) {
            val uri = withAppendedPath(CONTENT_FILTER_URI, encode(phone))
            val cursor = context.contentResolver.query(uri, arrayOf(DISPLAY_NAME), null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME))
                }
                cursor.close()
            } else {
                throw NullPointerException("Cannot obtain cursor")
            }
        }
        return result
    }

    fun createPickContactEmailIntent(): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = Email.CONTENT_TYPE
        return intent
    }

    fun emailAddressFromIntent(context: Context, intent: Intent?): String? {
        return intent?.data?.lastPathSegment?.let { emailAddress(context, it) }
    }

    fun phoneFromIntent(context: Context, intent: Intent?): String? {
        return intent?.data?.lastPathSegment?.let { phone(context, it) }
    }

    fun isReadContactsPermissionsDenied(context: Context): Boolean {
        return isPermissionsDenied(context, permission.READ_CONTACTS)
    }

    fun markSmsAsRead(context: Context, event: PhoneEvent) {
        val contentResolver = context.contentResolver
        val uri = Uri.parse("content://sms/inbox")
        val cursor = contentResolver.query(uri, null, "read = 0 AND address = ? AND date_sent = ?",
                arrayOf(event.phone, event.startTime.toString()), null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val id = cursor.getString(cursor.getColumnIndex("_id"))
                val values = ContentValues()
                values.put("read", true)
                values.put("seen", true)
                contentResolver.update(uri, values, "_id=$id", null)
                log.debug("SMS marked as read. $event")
            }
            cursor.close()
        } else {
            throw NullPointerException("Cannot obtain cursor")
        }
    }

    private fun emailAddress(context: Context, emailId: String): String? {
        var result: String? = null
        if (requireReadContactPermission(context) && emailId.isNotEmpty()) {
            val cursor = context.contentResolver.query(Email.CONTENT_URI, null,
                    Email._ID + "=" + emailId, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(Email.DATA))
                }
                cursor.close()
            } else {
                throw NullPointerException("Cannot obtain cursor")
            }
        }
        return result
    }

    private fun phone(context: Context, phoneId: String): String? {
        var result: String? = null
        if (requireReadContactPermission(context) && phoneId.isNotEmpty()) {
            val cursor = context.contentResolver.query(Phone.CONTENT_URI, null,
                    Phone._ID + "=" + phoneId, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(Phone.DATA))
                }
                cursor.close()
            } else {
                throw NullPointerException("Cannot obtain cursor")
            }
        }
        return result
    }

    private fun requireReadContactPermission(context: Context): Boolean {
        if (isReadContactsPermissionsDenied(context)) {
            log.warn("Unable read contact. Permission denied.")
            return false
        }
        return true
    }
}
