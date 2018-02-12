package axiom.com.audiobook.data;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import axiom.com.audiobook.R;

public class JsonUtils {
    public static List<Book> extractBooksFromJson(Context context, String output) {
        List<Book> bookList = new ArrayList<Book>();
        try {
            JSONObject bookObj = new JSONObject(output);
            JSONArray books = bookObj.getJSONArray("books");
            for (int i = 0; i < books.length(); i++) {
                JSONObject book = books.getJSONObject(i);
                String bookId = book.getString("id");
                String title = book.getString("title");
                String desc = book.getString("description");
                String urlZipFile = book.getString("url_zip_file");
                if (TextUtils.isEmpty(urlZipFile)) {
                    continue;
                }
                String[] imageUrls = getBookDetails(context,urlZipFile);
                String copyrightYear = book.getString("copyright_year");
                String totaltime = book.getString("totaltime");
                JSONArray authorArray = book.getJSONArray("authors");
                JSONObject author = authorArray.getJSONObject(0);
                String lastName = author.getString("last_name");
                String firstName = author.getString("first_name");
                String authorName = firstName.trim() + " " + lastName.trim();

                if (imageUrls == null || !NetworkUtils.isNetworkConnected(context)) {
                    return null;
                }
                bookList.add(new Book(imageUrls[0], imageUrls[1], bookId, title, desc, urlZipFile, authorName, copyrightYear, totaltime, null));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Intent jsonExceptionIntent = new Intent(Constants.ACTION_NO_BOOKS_FOUND);
            context.sendBroadcast(jsonExceptionIntent);
            return null;
        }
        return bookList;
    }

    private static String[] getBookDetails(Context context, String urlZipFileString) {
        int index = urlZipFileString.lastIndexOf('/');
        String temp = urlZipFileString.substring(0, index);
        int secindex = temp.lastIndexOf('/');
        String detailUrl = context.getString(R.string.detail_base_url) + temp.substring(secindex, temp.length()) + "?output=json";

        String jsonResponse = "";
        URL bookDetailsUrl = NetworkUtils.createUrl(detailUrl);
        try {
            jsonResponse = NetworkUtils.getResponseFromHttpUrl(bookDetailsUrl);
        } catch (IOException e) {
            e.printStackTrace();
            Intent jsonExceptionIntent = new Intent(Constants.ACTION_NO_BOOKS_FOUND);
            context.sendBroadcast(jsonExceptionIntent);
            return null;
        }
        try {
            return JsonUtils.extractImagesFromJson(context,jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
            Intent jsonExceptionIntent = new Intent(Constants.ACTION_NO_BOOKS_FOUND);
            context.sendBroadcast(jsonExceptionIntent);
            return null;
        }
    }

    public static String[] extractImagesFromJson(Context context,String jsonResponse) throws JSONException {
        String[] imageUrlArray = new String[2];

        try {
            JSONObject detailsObj = new JSONObject(jsonResponse);
            String server = detailsObj.getString("server");
            String dir = detailsObj.getString("dir");
            JSONObject filesObj = detailsObj.getJSONObject("files");
            Iterator<String> iterator = filesObj.keys();
            int i = 0;
            while (iterator.hasNext()) {
                String key = iterator.next();
                int index = key.indexOf(".");
                String temp = key.substring(index + 1);
                if (temp.equals("jpg") && i < 2) {
                    imageUrlArray[i] = "https://" + server + dir + key;
                    i++;
                }
                if (i == 2) {
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Intent jsonExceptionIntent = new Intent(Constants.ACTION_NO_BOOKS_FOUND);
            context.sendBroadcast(jsonExceptionIntent);
            return null;
        }
        return imageUrlArray;
    }

    public static List<Chapter> extractChaptersFromJson(String output) {
        ArrayList<Chapter> chapterList = null;
        try {
            JSONObject bookObj = new JSONObject(output);
            Object booksAsObject = bookObj.get("books");
            if (booksAsObject instanceof JSONArray) {
                JSONArray books = bookObj.getJSONArray("books");
                for (int i = 0; i < books.length(); i++) {
                    JSONObject book = books.getJSONObject(i);
                    chapterList = getChapterListFromBook(book);
                }
            } else if (booksAsObject instanceof JSONObject) {
                JSONObject books = bookObj.getJSONObject("books");
                Iterator<String> iterator = books.keys();
                while (iterator.hasNext()) {
                    JSONObject book = books.getJSONObject(iterator.next());
                    chapterList = getChapterListFromBook(book);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chapterList;
    }

    private static ArrayList<Chapter> getChapterListFromBook(JSONObject book) {
        ArrayList<Chapter> chapterList = new ArrayList<>();
        try {
            String bookId = book.getString("id");
            JSONArray sectionArray = book.getJSONArray("sections");
            for (int j = 0; j < sectionArray.length(); j++) {
                JSONObject chapterObj = sectionArray.getJSONObject(j);
                String chapterId = chapterObj.getString("id");
                String chapterTitle = chapterObj.getString("title");
                String playtime = chapterObj.getString("playtime");
                chapterList.add(new Chapter(bookId, chapterId, chapterTitle, playtime));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chapterList;
    }
}
