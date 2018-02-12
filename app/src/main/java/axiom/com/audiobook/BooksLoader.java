package axiom.com.audiobook;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import axiom.com.audiobook.data.Book;
import axiom.com.audiobook.data.Constants;
import axiom.com.audiobook.data.JsonUtils;
import axiom.com.audiobook.data.NetworkUtils;

public class BooksLoader extends AsyncTaskLoader<List<Book>> {
    private String mUrlString;
    private Context mContext;

    public BooksLoader(Context context, String urlString) {
        super(context);
        mContext = context;
        mUrlString = urlString;
    }

    @Override
    public void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground() {
        URL booksUrl = NetworkUtils.createUrl(mUrlString);
        String jsonResponse = "";
        try {
            jsonResponse = NetworkUtils.getResponseFromHttpUrl(booksUrl);
        } catch (IOException e) {
            e.printStackTrace();
            Intent ioExceptionIntent = new Intent(Constants.ACTION_NO_BOOKS_FOUND);
            mContext.sendBroadcast(ioExceptionIntent);
            return null;
        }
        return JsonUtils.extractBooksFromJson(getContext(), jsonResponse);
    }
}
