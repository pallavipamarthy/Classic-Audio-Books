package axiom.com.audiobook.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import axiom.com.audiobook.data.BookContract.BookEntry;
import axiom.com.audiobook.data.BookContract.CatalogEntry;
import axiom.com.audiobook.data.BookContract.FavouriteEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "audiobooks.db";
    private static final int DATABASE_VERSION = 1;

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String SQ_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + "(" + BookEntry._ID + " INTEGER PRIMARY KEY," + BookEntry.COLUMN_BOOK_IMAGE_URL + " TEXT NOT NULL," + BookEntry.COLUMN_BOOK_THUMBNAIL + " TEXT NOT NULL," + BookEntry.COLUMN_BOOK_ID + " TEXT NOT NULL," + BookEntry.COLUMN_BOOK_TITLE + " TEXT NOT NULL,"
                + BookEntry.COLUMN_BOOK_DESC + " TEXT NOT NULL," + BookEntry.COLUMN_BOOK_ABSOLUTE_PATH + " TEXT NOT NULL," + BookEntry.COLUMN_BOOK_AUTHOR + " TEXT NOT NULL," +
                BookEntry.COLUMN_BOOK_YEAR + " TEXT NOT NULL," + BookEntry.COLUMN_BOOK_DURATION + " TEXT NOT NULL);";
        db.execSQL(SQ_CREATE_BOOKS_TABLE);

        String SQ_CREATE_CATALOG_TABLE = "CREATE TABLE " + CatalogEntry.TABLE_NAME + "(" + CatalogEntry._ID + " INTEGER PRIMARY KEY," + CatalogEntry.COLUMN_BOOK_IMAGE_URL + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_THUMBNAIL + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_ID + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_TITLE + " TEXT NOT NULL,"
                + CatalogEntry.COLUMN_BOOK_GENRE + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_DESC + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_DOWNLOAD_URL + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_AUTHOR + " TEXT NOT NULL," +
                CatalogEntry.COLUMN_BOOK_YEAR + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_DURATION + " TEXT NOT NULL);";
        db.execSQL(SQ_CREATE_CATALOG_TABLE);

       String SQ_CREATE_FAVOURITES_TABLE = "CREATE TABLE " + FavouriteEntry.TABLE_NAME + "(" + FavouriteEntry._ID + " INTEGER PRIMARY KEY," + CatalogEntry.COLUMN_BOOK_IMAGE_URL + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_THUMBNAIL + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_ID + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_TITLE + " TEXT NOT NULL,"
                + CatalogEntry.COLUMN_BOOK_DESC + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_DOWNLOAD_URL + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_AUTHOR + " TEXT NOT NULL," +
                CatalogEntry.COLUMN_BOOK_YEAR + " TEXT NOT NULL," + CatalogEntry.COLUMN_BOOK_DURATION + " TEXT NOT NULL);";

        db.execSQL(SQ_CREATE_FAVOURITES_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CatalogEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteEntry.TABLE_NAME);
    }
}
