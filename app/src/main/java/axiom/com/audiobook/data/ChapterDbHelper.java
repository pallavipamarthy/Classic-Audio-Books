package axiom.com.audiobook.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import axiom.com.audiobook.data.ChapterContract.ChapterEntry;

public class ChapterDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chapters.db";
    private static final int DATABASE_VERSION = 1;

    public ChapterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String SQ_CREATE_CHAPTERS_TABLE = "CREATE TABLE " + ChapterEntry.TABLE_NAME + "(" + ChapterEntry._ID + " INTEGER PRIMARY KEY," + ChapterEntry.COLUMN_BOOK_ID + " TEXT NOT NULL," + ChapterEntry.COLUMN_CHAPTER_ID + " TEXT NOT NULL," + ChapterEntry.COLUMN_CHAPTER_TITLE + " TEXT NOT NULL," + ChapterEntry.COLUMN_PLAYTIME + " TEXT NOT NULL);";
        db.execSQL(SQ_CREATE_CHAPTERS_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
