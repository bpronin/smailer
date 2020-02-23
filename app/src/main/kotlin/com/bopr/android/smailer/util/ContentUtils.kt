package com.bopr.android.smailer.util

import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.READ_SMS
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.Uri.encode
import android.net.Uri.withAppendedPath
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.PhoneLookup
import androidx.annotation.RequiresPermission
import com.bopr.android.smailer.PermissionsHelper.Companion.WRITE_SMS
import com.bopr.android.smailer.PhoneEvent

@RequiresPermission(READ_CONTACTS, conditional = true)
fun contactName(context: Context, phone: String): String? {
    val uri = withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, encode(phone))
    var result: String? = null
    context.contentResolver.query(uri, arrayOf(PhoneLookup.DISPLAY_NAME), null, null, null)?.use {
        if (it.moveToFirst()) {
            result = it.getString(it.getColumnIndex(PhoneLookup.DISPLAY_NAME))
        }
    }
    return result
}

@RequiresPermission(READ_CONTACTS, conditional = true)
fun createPickContactIntent(): Intent {
    return Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI)
}

@RequiresPermission(READ_CONTACTS, conditional = true)
fun phoneFromIntent(context: Context, intent: Intent?): String? {
    val uri: Uri = intent!!.data!!
    var result: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use {
        if (it.moveToFirst()) {
            val hasPhone = it.getInt(it.getColumnIndex(Contacts.HAS_PHONE_NUMBER))
            if (hasPhone == 1) {
                val id = it.getString(it.getColumnIndexOrThrow(Contacts._ID))
                result = retrievePhone(context, id)
            }
        }
    }
    return result
}

@RequiresPermission(READ_CONTACTS, conditional = true)
fun emailFromIntent(context: Context, intent: Intent?): String? {
    val uri: Uri = intent!!.data!!
    var result: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use {
        if (it.moveToFirst()) {
            val id = it.getString(it.getColumnIndexOrThrow(Contacts._ID))
            result = retrieveEmail(context, id)
        }
    }
    return result
}

@RequiresPermission(anyOf = [READ_SMS, WRITE_SMS], conditional = true)
fun markSmsAsRead(context: Context, event: PhoneEvent) {
    val uri = Uri.parse("content://sms/inbox")
    val resolver = context.contentResolver
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

private fun retrievePhone(context: Context, contactId: String): String? {
    var result: String? = null
    context.contentResolver.query(Phone.CONTENT_URI, null, "${Phone.CONTACT_ID} = $contactId", null, null)?.use {
        if (it.moveToFirst()) {
            result = it.getString(it.getColumnIndex(Phone.DATA))
        }
    }
    return result
}

private fun retrieveEmail(context: Context, contactId: String): String? {
    var result: String? = null
    context.contentResolver.query(Email.CONTENT_URI, null, "${Email.CONTACT_ID}=$contactId", null, null)?.use {
        if (it.moveToFirst()) {
            result = it.getString(it.getColumnIndex(Email.DATA))
        }
    }
    return result
}
