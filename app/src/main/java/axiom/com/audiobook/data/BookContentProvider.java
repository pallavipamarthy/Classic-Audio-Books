package axiom.com.audiobook.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import axiom.com.audiobook.data.BookContract.BookEntry;
import axiom.com.audiobook.data.BookContract.CatalogEntry;
import axiom.com.audiobook.data.BookContract.FavouriteEntry;

public class BookContentProvider extends android.content.ContentProvider {

    public static final String LOG_TAG = BookContentProvider.class.getSimpleName();
    private static final int BOOKS = 100;
    private static final int BOOK_ID = 101;
    private static final int CATALOG = 102;
    private static final int CATALOG_ID = 103;
    private static final int FAVOURITES = 104;
    private static final int FAVOURITES_ID = 105;
    private BookDbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(BookContract.AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        sUriMatcher.addURI(BookContract.AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
        sUriMatcher.addURI(BookContract.AUTHORITY, BookContract.PATH_CATALOG, CATALOG);
        sUriMatcher.addURI(BookContract.AUTHORITY, BookContract.PATH_CATALOG + "/#", CATALOG_ID);
        sUriMatcher.addURI(BookContract.AUTHORITY, BookContract.PATH_FAVOURITES, FAVOURITES);
        sUriMatcher.addURI(BookContract.AUTHORITY, BookContract.PATH_FAVOURITES + "/#", FAVOURITES_ID);
        return sUriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new BookDbHelper(getContext());
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
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(BookEntry.TABLE_NAME, null, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CATALOG:
                cursor = database.query(CatalogEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case CATALOG_ID:
                selection = CatalogEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(CatalogEntry.TABLE_NAME, null, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case FAVOURITES:
                cursor = database.query(FavouriteEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case FAVOURITES_ID:
                selection = FavouriteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(CatalogEntry.TABLE_NAME, null, selection, selectionArgs,
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
            case BOOKS:
                return insertBook(uri, contentValues);
            case CATALOG:
                return insertCatalogBook(uri, contentValues);
            case FAVOURITES:
                return insertFavouriteBook(uri, contentValues);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private Uri insertBook(Uri uri, ContentValues values) {
        Uri insertUri;
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(BookEntry.TABLE_NAME, null, values);
        if (id > 0) {
            insertUri = ContentUris.withAppendedId(uri, id);
        } else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertUri;
    }

    private Uri insertCatalogBook(Uri uri, ContentValues values) {
        Uri insertUri;
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(CatalogEntry.TABLE_NAME, null, values);
        if (id > 0) {
            insertUri = ContentUris.withAppendedId(uri, id);
        } else {
            throw new android.database.SQLException("Failed to insert row into " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return insertUri;
    }

    private Uri insertFavouriteBook(Uri uri, ContentValues values) {
        Uri insertUri;
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(FavouriteEntry.TABLE_NAME, null, values);
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
            case BOOKS:
                return 0;
            case CATALOG:
                return 0;
            case FAVOURITES:
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
            case BOOKS:
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATALOG:
                rowsDeleted = database.delete(CatalogEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATALOG_ID:
                selection = CatalogEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(CatalogEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FAVOURITES:
                rowsDeleted = database.delete(FavouriteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FAVOURITES_ID:
                selection = FavouriteEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(FavouriteEntry.TABLE_NAME, selection, selectionArgs);
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
            case BOOKS:
                return null;
            case BOOK_ID:
                return null;
            case CATALOG:
                return null;
            case CATALOG_ID:
                return null;
            case FAVOURITES:
                return null;
            case FAVOURITES_ID:
                return null;
            default:
                throw new IllegalArgumentException("Type is not supported for " + uri);
        }
    }
}
