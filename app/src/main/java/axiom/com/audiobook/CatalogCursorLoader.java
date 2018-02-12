package axiom.com.audiobook;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

import axiom.com.audiobook.data.Book;
import axiom.com.audiobook.data.BookContract;
import axiom.com.audiobook.data.BookContract.CatalogEntry;
import axiom.com.audiobook.data.BookContract.FavouriteEntry;
import axiom.com.audiobook.data.BookDbHelper;
import axiom.com.audiobook.data.Chapter;
import axiom.com.audiobook.data.ChapterContract;
import axiom.com.audiobook.data.ChapterDbHelper;

public class CatalogCursorLoader extends AsyncTaskLoader<List<Book>> {
    private Context mContext;
    private String mGenre;

    public CatalogCursorLoader(Context context, String genre) {
        super(context);
        mContext = context;
        mGenre = genre;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground() {

        List<Book> catalogbookList = new ArrayList<>();
        if (mGenre!=null) {
            Cursor cursor;
            if(mGenre.equals("Favorites")) {
                 cursor = getContext().getContentResolver().query(FavouriteEntry.CONTENT_URI,
                        null, null, null, null);
            } else {
                String selection = CatalogEntry.COLUMN_BOOK_GENRE + "=?";
                String[] selectionArgs = {mGenre};
                cursor = getContext().getContentResolver().query(CatalogEntry.CONTENT_URI,
                        null, selection, selectionArgs, null);
            }
            while (cursor.moveToNext()) {
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(CatalogEntry.COLUMN_BOOK_IMAGE_URL));
                String thumbnail = cursor.getString(cursor.getColumnIndexOrThrow(CatalogEntry.COLUMN_BOOK_THUMBNAIL));
                String bookId = cursor.getString(cursor.getColumnIndexOrThrow(CatalogEntry.COLUMN_BOOK_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(CatalogEntry.COLUMN_BOOK_TITLE));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(CatalogEntry.COLUMN_BOOK_DESC));
                String downloadUrl = cursor.getString(cursor.getColumnIndexOrThrow(CatalogEntry.COLUMN_BOOK_DOWNLOAD_URL));
                String author = cursor.getString(cursor.getColumnIndexOrThrow(CatalogEntry.COLUMN_BOOK_AUTHOR));
                String year = cursor.getString(cursor.getColumnIndexOrThrow(CatalogEntry.COLUMN_BOOK_YEAR));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(CatalogEntry.COLUMN_BOOK_DURATION));
                catalogbookList.add(new Book(imageUrl, thumbnail, bookId, title, desc, downloadUrl, author, year, duration, null));
            }
            cursor.close();
            return catalogbookList;
        } else {
            List<Book> downloadBookList = new ArrayList<>();
            ArrayList<Chapter> chapterList;
            Cursor cursor = getContext().getContentResolver().query(BookContract.BookEntry.CONTENT_URI,
                    null, null, null, null);

            while (cursor.moveToNext()) {
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_IMAGE_URL));
                String thumbnail = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_THUMBNAIL));
                String bookId = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_TITLE));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_DESC));
                String absolutePath = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_ABSOLUTE_PATH));
                String author = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_AUTHOR));
                String year = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_YEAR));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_DURATION));
                chapterList = new ArrayList<>();
                ChapterDbHelper chapDbHelper = new ChapterDbHelper(mContext);
                chapDbHelper.getReadableDatabase();
                String selection = ChapterContract.ChapterEntry.COLUMN_BOOK_ID + "=?";
                String[] selectionArgs = {bookId};
                Cursor chapCursor = mContext.getContentResolver().query(ChapterContract.ChapterEntry.CONTENT_URI, null, selection, selectionArgs, null);

                while (chapCursor.moveToNext()) {
                    String id = chapCursor.getString(chapCursor.getColumnIndexOrThrow(ChapterContract.ChapterEntry.COLUMN_BOOK_ID));
                    String chapterId = chapCursor.getString(chapCursor.getColumnIndexOrThrow(ChapterContract.ChapterEntry.COLUMN_CHAPTER_ID));
                    String chapterName = chapCursor.getString(chapCursor.getColumnIndexOrThrow(ChapterContract.ChapterEntry.COLUMN_CHAPTER_TITLE));
                    String playtime = chapCursor.getString(chapCursor.getColumnIndexOrThrow(ChapterContract.ChapterEntry.COLUMN_PLAYTIME));
                    chapterList.add(new Chapter(id, chapterId, chapterName, playtime));
                }
                chapCursor.close();

                downloadBookList.add(new Book(imageUrl, thumbnail, bookId, title, desc, absolutePath, author, year, duration, chapterList));
            }
            cursor.close();
            return downloadBookList;
        }
    }
}
