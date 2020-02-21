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

fun contactName(context: Context, phone: String): String? {
    var result: String? = null
    if (hasReadContactPermission(context) && phone.isNotEmpty()) {
        val uri = withAppendedPath(CONTENT_FILTER_URI, encode(phone))
        context.contentResolver.query(uri, arrayOf(DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(DISPLAY_NAME))
            }
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
    return intent?.data?.lastPathSegment?.let { getEmailAddress(context, it) }
}

fun phoneFromIntent(context: Context, intent: Intent?): String? {
    return intent?.data?.lastPathSegment?.let { getPhone(context, it) }
}

fun isReadContactsPermissionsDenied(context: Context): Boolean {
    return isPermissionsDenied(context, permission.READ_CONTACTS)
}

fun markSmsAsRead(context: Context, event: PhoneEvent) {
    val resolver = context.contentResolver
    val uri = Uri.parse("content://sms/inbox")
    resolver.query(uri, null, "read = 0 AND address = ? AND date_sent = ?",
            arrayOf(event.phone, event.startTime.toString()), null)?.use {
        if (it.moveToFirst()) {
            val id = it.getString(it.getColumnIndex("_id"))
            val values = ContentValues()
            values.put("read", true)
            values.put("seen", true)
            resolver.update(uri, values, "_id=$id", null)
        }
    }
}

private fun getEmailAddress(context: Context, emailId: String): String? {
    var result: String? = null
    if (hasReadContactPermission(context) && emailId.isNotEmpty()) {
        context.contentResolver.query(Email.CONTENT_URI, null, Email._ID + "=" + emailId, null, null)?.use {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(Email.DATA))
            }
        }
    }
    return result
}

private fun getPhone(context: Context, phoneId: String): String? {
    var result: String? = null
    if (hasReadContactPermission(context) && phoneId.isNotEmpty()) {
        context.contentResolver.query(Phone.CONTENT_URI, null, Phone._ID + "=" + phoneId, null, null)?.use {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndex(Phone.DATA))
            }
        }
    }
    return result
}

private fun hasReadContactPermission(context: Context): Boolean {
    if (isReadContactsPermissionsDenied(context)) {
        return false
    }
    return true
}