package com.bopr.android.smailer.util

import android.Manifest.permission.READ_CONTACTS
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
import com.bopr.android.smailer.util.database.getInt
import com.bopr.android.smailer.util.database.getString

@RequiresPermission(READ_CONTACTS)
fun contactName(context: Context, phone: String): String? {
    val uri = withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, encode(phone))
    var result: String? = null
    context.contentResolver.query(uri, arrayOf(PhoneLookup.DISPLAY_NAME), null, null, null)?.use {
        if (it.moveToFirst()) {
            result = it.getString(PhoneLookup.DISPLAY_NAME)
        }
    }
    return result
}

@RequiresPermission(READ_CONTACTS)
fun createPickContactIntent(): Intent {
    return Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI)
}

@RequiresPermission(READ_CONTACTS)
fun phoneFromIntent(context: Context, intent: Intent?): String? {
    val uri: Uri = intent!!.data!!
    var result: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use {
        if (it.moveToFirst()) {
            val hasPhone = it.getInt(Contacts.HAS_PHONE_NUMBER)
            if (hasPhone == 1) {
                val id = it.getString(it.getColumnIndexOrThrow(Contacts._ID))
                result = retrievePhone(context, id)
            }
        }
    }
    return result
}

@RequiresPermission(READ_CONTACTS)
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

private fun retrievePhone(context: Context, contactId: String): String? {
    var result: String? = null
    context.contentResolver.query(Phone.CONTENT_URI, null, "${Phone.CONTACT_ID} = $contactId",
            null, null)?.use {
        if (it.moveToFirst()) {
            result = it.getString(Phone.DATA)
        }
    }
    return result
}

private fun retrieveEmail(context: Context, contactId: String): String? {
    var result: String? = null
    context.contentResolver.query(Email.CONTENT_URI, null, "${Email.CONTACT_ID}=$contactId",
            null, null)?.use {
        if (it.moveToFirst()) {
            result = it.getString(Email.DATA)
        }
    }
    return result
}
