package com.bopr.android.smailer.util

import android.Manifest.permission.READ_CONTACTS
import android.accounts.Account
import android.accounts.AccountManager.newChooseAccountIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.Uri.encode
import android.net.Uri.withAppendedPath
import android.os.Build
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.PhoneLookup
import androidx.annotation.RequiresPermission
import com.bopr.android.smailer.data.getInt
import com.bopr.android.smailer.data.getStringOrNull
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager.ACCOUNT_TYPE

fun createPickAccountIntent(account: Account?): Intent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        newChooseAccountIntent(account, null, arrayOf(ACCOUNT_TYPE), null, null, null, null)
    } else {
        @Suppress("DEPRECATION")
        newChooseAccountIntent(account, null, arrayOf(ACCOUNT_TYPE), false, null, null, null, null)
    }

}

@RequiresPermission(READ_CONTACTS)
fun createPickContactIntent(): Intent {
    return Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI)
}

fun Context.getContactName(phone: String): String? {
    if (!checkPermission(READ_CONTACTS)) return null

    val uri = withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, encode(phone))
    contentResolver.query(uri, arrayOf(PhoneLookup.DISPLAY_NAME), null, null, null)?.use {
        if (it.moveToFirst()) {
            return it.getStringOrNull(PhoneLookup.DISPLAY_NAME)
        }
    }

    return null
}

@RequiresPermission(READ_CONTACTS)
fun Context.phoneFromIntent(intent: Intent?): String? {
    val uri: Uri = intent!!.data!!
    var result: String? = null
    contentResolver.query(uri, null, null, null, null)?.use {
        if (it.moveToFirst()) {
            val hasPhone = it.getInt(Contacts.HAS_PHONE_NUMBER)
            if (hasPhone == 1) {
                val id = it.getString(it.getColumnIndexOrThrow(Contacts._ID))
                result = retrievePhone(id)
            }
        }
    }
    return result
}

@RequiresPermission(READ_CONTACTS)
fun Context.emailFromIntent(intent: Intent?): String? {
    val uri: Uri = intent!!.data!!
    var result: String? = null
    contentResolver.query(uri, null, null, null, null)?.use {
        if (it.moveToFirst()) {
            val id = it.getString(it.getColumnIndexOrThrow(Contacts._ID))
            result = retrieveEmail(id)
        }
    }
    return result
}

private fun Context.retrievePhone(contactId: String): String? {
    var result: String? = null
    contentResolver.query(
        Phone.CONTENT_URI, null, "${Phone.CONTACT_ID} = $contactId",
        null, null
    )?.use {
        if (it.moveToFirst()) {
            result = it.getStringOrNull(Phone.DATA)
        }
    }
    return result
}

private fun Context.retrieveEmail(contactId: String): String? {
    var result: String? = null
    contentResolver.query(
        Email.CONTENT_URI, null, "${Email.CONTACT_ID}=$contactId",
        null, null
    )?.use {
        if (it.moveToFirst()) {
            result = it.getStringOrNull(Email.DATA)
        }
    }
    return result
}
