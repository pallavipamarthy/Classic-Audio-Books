package axiom.com.audiobook.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import axiom.com.audiobook.data.ChapterContract.ChapterEntry;

public class ChapterContentProvider extends ContentProvider {

    public static final String LOG_TAG = ChapterContentProvider.class.getSimpleName();
    private static final int CHAPTERS = 100;
    private static final int CHAPTER_ID = 101;
    private ChapterDbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(ChapterContract.AUTHORITY, ChapterContract.PATH_CHAPTERS, CHAPTERS);
        sUriMatcher.addURI(ChapterContract.AUTHORITY, ChapterContract.PATH_CHAPTERS + "/#", CHAPTER_ID);
        return sUriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new ChapterDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        Cursor cursor;
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CHAPTERS:
                cursor = database.query(ChapterEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case CHAPTER_ID:
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ChapterEntry.TABLE_NAME, null, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHAPTERS:
                return insertBook(uri, contentValues);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {
        Uri insertUri;
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(ChapterEntry.TABLE_NAME, null, values);
        if (id > 0) {
            insertUri = ContentUris.withAppendedId(uri, id);
        } else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertUri;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHAPTERS:
                return 0;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHAPTERS:
                rowsDeleted = database.delete(ChapterEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CHAPTER_ID:
                selection = ChapterEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ChapterEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CHAPTERS:
                return null;
            case CHAPTER_ID:
                return null;
            default:
                throw new IllegalArgumentException("Type is not supported for " + uri);
        }
    }
}
