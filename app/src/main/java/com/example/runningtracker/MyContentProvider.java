package com.example.runningtracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyContentProvider extends ContentProvider {

    DBHelper dbHelper;
    private static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ProviderContract.AUTHORITY, "location", 1);
        uriMatcher.addURI(ProviderContract.AUTHORITY, "location/#",2);
    }

    @Override
    public boolean onCreate() {
        Log.d("ContentProvider", "ContentProvider created");
        this.dbHelper = new DBHelper(this.getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return dbHelper.getReadableDatabase().query("RunningTrackerDB", projection, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        if(uri.getLastPathSegment()==null){
            return "vnd.android.cursor.dir/MyContentProvider.data.text";
        } else {
            return "vnd.android.cursor.item/MyContentProvider.data.text";
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        //Insert a run into the DB
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert("RunningTrackerDB", null, values);
        Uri uri1 = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(uri1, null);
        Log.d("ContentProvider", uri1.toString());
        return uri1;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return dbHelper.getWritableDatabase().delete("RunningTrackerDB", selection, null);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return dbHelper.getWritableDatabase().update("RunningTrackerDB", values, selection, null);
    }
}
