package com.bopr.android.smailer.sync

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.bopr.android.smailer.Database

class AppContentProvider : ContentProvider() {

    //    public static final Uri CONTENT_EVENTS = Uri.withAppendedPath(CONTENT, "events");
    private lateinit var database: Database

    override fun onCreate(): Boolean {
        database = Database(context)
        return true
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        //        if (isValidUri(uri)) {
        //            return database.query(uri.getLastPathSegment(), projection, selection, selectionArgs, sortOrder);
        //        }
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        //        if (isValidUri(uri)) {
        //            database.put(uri.getLastPathSegment(), values);
        //            return uri;
        //        }
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        //        if (isValidUri(uri)) {
        //            return database.delete(uri.getLastPathSegment(), selection, selectionArgs);
        //        }
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        //        if (isValidUri(uri)) {
        //            return database.update(uri.getLastPathSegment(), values, selection, selectionArgs);
        //        }
        return 0
    }

/*
    private fun isValidUri(uri: Uri): Boolean {
        return AUTHORITY == uri.authority
    }
*/

    companion object {
        const val AUTHORITY = "com.bopr.android.smailer"
//        val CONTENT = Uri.parse("content://$AUTHORITY/")
    }
}