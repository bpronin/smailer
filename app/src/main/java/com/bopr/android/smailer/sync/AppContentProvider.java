package com.bopr.android.smailer.sync;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.bopr.android.smailer";
    public static final Uri CONTENT = Uri.parse("content://" + AUTHORITY + "/");
//    public static final Uri CONTENT_EVENTS = Uri.withAppendedPath(CONTENT, "events");

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    protected boolean isValidUri(@NonNull Uri uri) {
        return AUTHORITY.equals(uri.getAuthority());
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
//        if (isValidUri(uri)) {
//            return database.query(uri.getLastPathSegment(), projection, selection, selectionArgs, sortOrder);
//        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
//        if (isValidUri(uri)) {
//            database.put(uri.getLastPathSegment(), values);
//            return uri;
//        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
//        if (isValidUri(uri)) {
//            return database.delete(uri.getLastPathSegment(), selection, selectionArgs);
//        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
//        if (isValidUri(uri)) {
//            return database.update(uri.getLastPathSegment(), values, selection, selectionArgs);
//        }
        return 0;
    }
}
