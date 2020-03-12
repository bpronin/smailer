package com.bopr.android.smailer.sync

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Application content provider. Does nothing.
 *
 * Required by synchronization framework.
 * Without it sync adapter won't call [SyncAdapter.onPerformSync]
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class AppContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        return 0
    }

    companion object {

        const val AUTHORITY = "com.bopr.android.smailer"
    }
}