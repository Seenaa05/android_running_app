package com.example.main;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.UriMatcher;
import android.net.Uri;
import android.util.Log;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;


public class JourneyProvider extends ContentProvider {
    DBCreator dbh;
    SQLiteDatabase db;

    private static final UriMatcher matcher;

    // map URI's to codes so we can decide which query to make based on the URI received
    static {
        Log.d("JCP", "URI mapped");
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(JPC.AUTHORITY, "journey", 1);
        matcher.addURI(JPC.AUTHORITY, "journey/#", 2);
        matcher.addURI(JPC.AUTHORITY, "location", 3);
        matcher.addURI(JPC.AUTHORITY, "location/#", 4);
    }

    @Override
    public boolean onCreate() {

        try {
            dbh = new DBCreator(this.getContext());
            db = dbh.getWritableDatabase();
            return (db != null);
        }catch (Exception e){
            Log.e("Error", "Failed to create DB");
        }
        return false;
    }

    @Override
    public String getType(Uri uri) {
        if (uri.getLastPathSegment() == null) {
            return "vnd.android.cursor.dir/JourneyProvider.data.text";
        } else {
            return "vnd.android.cursor.item/JourneyProvider.data.text";
        }
    }

    // implement CRUD database operations

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName;

        int matchResult = matcher.match(uri);
        if (matchResult == 1) {
            tableName = "journey";
        } else if (matchResult == 3) {
            tableName = "location";
        } else {
            tableName = "";
        }


        // insert the values into the table and return the same url but with the id appended
        long _id = db.insert(tableName, null, values);
        Uri newRowUri = ContentUris.withAppendedId(uri, _id);

        Log.d("JCP", "Change made");
        getContext().getContentResolver().notifyChange(newRowUri, null);
        return newRowUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[]
            selectionArgs, String sortOrder) {

        int match = matcher.match(uri);
        if (match == 2) {// gave /# URI so they want a specific row
            selection = "journeyID = " + uri.getLastPathSegment();

            return db.query("journey", projection, selection, selectionArgs, null, null, sortOrder);
        } else if (match == 1) {
            return db.query("journey", projection, selection, selectionArgs, null, null, sortOrder);
        } else if (match == 4) {
            selection = "locationID = " + uri.getLastPathSegment();

            return db.query("location", projection, selection, selectionArgs, null, null, sortOrder);
        } else if (match == 3) {
            return db.query("location", projection, selection, selectionArgs, null, null, sortOrder);
        }
        return null;


    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        String tableName;
        int count;


        int matchResult = matcher.match(uri);
        if (matchResult == 2 || matchResult == 1) {
            // gave /# URI so they want a specific row
            selection = "journeyID = " + uri.getLastPathSegment();
            tableName = "journey";
            count = db.update(tableName, values, selection, selectionArgs);
        } else if (matchResult == 4 || matchResult == 3) {
            selection = "locationID = " + uri.getLastPathSegment();
            tableName = "location";
            count = db.update(tableName, values, selection, selectionArgs);
        } else {
            return 0;
        }


        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tableName;
        int count;

        // given uri -> table name
        int matchResult = matcher.match(uri);
        if (matchResult == 2 || matchResult == 1) {
            // gave /# URI so they want a specific row
            selection = "journeyID = " + uri.getLastPathSegment();
            tableName = "journey";
            count = db.delete(tableName, selection, selectionArgs);
        } else if (matchResult == 4 || matchResult == 3) {
            selection = "locationID = " + uri.getLastPathSegment();
            tableName = "location";
            count = db.delete(tableName, selection, selectionArgs);
        } else {
            return 0;
        }


        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
