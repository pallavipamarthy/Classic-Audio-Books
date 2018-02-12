package axiom.com.audiobook;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import axiom.com.audiobook.data.Book;
import axiom.com.audiobook.data.BookContract.CatalogEntry;
import axiom.com.audiobook.data.Constants;
import axiom.com.audiobook.data.JsonUtils;
import axiom.com.audiobook.data.NetworkUtils;
import axiom.com.audiobook.data.PrefUtils;

public class CatalogFetchIntentService extends IntentService {
    public CatalogFetchIntentService() {
        super(CatalogFetchIntentService.class.getSimpleName());
    }

    private String mGenre = "";

    @Override
    protected void onHandleIntent(Intent intent) {
        List<Book> bookList = null;
        String urlString = getUrlString(intent);

        Log.e("##########","onHandleIntent -  " +intent.getStringExtra(getString(R.string.genre_extra_text)));

        URL createdUrl = NetworkUtils.createUrl(urlString);
        String jsonResponse = "";
        try {
            jsonResponse = NetworkUtils.getResponseFromHttpUrl(createdUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        bookList = JsonUtils.extractBooksFromJson(this, jsonResponse);
        if (bookList != null) {
            for (int i = 0; i < bookList.size(); i++) {
                addBook(bookList.get(i));
            }
            Intent downloadFinishIntent = new Intent(Constants.ACTION_CATALOG_DOWNLOAD_FINISHED);
            downloadFinishIntent.putExtra(getString(R.string.genre_extra_text), mGenre);
            sendBroadcast(downloadFinishIntent);
            PrefUtils.setValueForGenre(this, mGenre, true);
        }
    }

    public void addBook(Book currentBook) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CatalogEntry.COLUMN_BOOK_IMAGE_URL, currentBook.getImageUrl());
        contentValues.put(CatalogEntry.COLUMN_BOOK_THUMBNAIL, currentBook.getThumbNailUrl());
        contentValues.put(CatalogEntry.COLUMN_BOOK_ID, currentBook.getBookId());
        contentValues.put(CatalogEntry.COLUMN_BOOK_TITLE, currentBook.getTitle());
        contentValues.put(CatalogEntry.COLUMN_BOOK_GENRE, mGenre);
        contentValues.put(CatalogEntry.COLUMN_BOOK_DESC, currentBook.getDescription());
        contentValues.put(CatalogEntry.COLUMN_BOOK_DOWNLOAD_URL, currentBook.getZipFileUrlString());
        contentValues.put(CatalogEntry.COLUMN_BOOK_AUTHOR, currentBook.getAuthorName());
        contentValues.put(CatalogEntry.COLUMN_BOOK_YEAR, currentBook.getCopyrightYear());
        contentValues.put(CatalogEntry.COLUMN_BOOK_DURATION, currentBook.getTotalTime());
        getContentResolver().insert(CatalogEntry.CONTENT_URI, contentValues);
    }

    public String getUrlString(Intent intent) {
        String urlString = "";
        mGenre = intent.getStringExtra(getString(R.string.genre_extra_text));
        if (mGenre.equals(Constants.ROMANCE_GENRE)) {
            urlString = Constants.BOOK_ROMANCE_GENRE_QUERY;
        } else if (mGenre.equals(Constants.COMEDY_GENRE)) {
            urlString = Constants.BOOK_COMEDY_GENRE_QUERY;
        } else if (mGenre.equals(Constants.DRAMA_GENRE)) {
            urlString = Constants.BOOK_DRAMA_GENRE_QUERY;
        }
        return urlString;
    }
}
