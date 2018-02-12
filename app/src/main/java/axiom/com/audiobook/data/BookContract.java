package axiom.com.audiobook.data;


import android.net.Uri;
import android.provider.BaseColumns;

public class BookContract {
    public static final String AUTHORITY = "com.example.android.bookdatabase";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_BOOKS = "audiobooks";
    public static final String PATH_CATALOG = "book_catalog";
    public static final String PATH_FAVOURITES = "favourite_books";

    private BookContract() {
    }

    public static final class BookEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();

        public final static String TABLE_NAME = "audiobooks";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_BOOK_IMAGE_URL = "imageUrl";
        public final static String COLUMN_BOOK_THUMBNAIL = "thumbnailUrl";
        public final static String COLUMN_BOOK_ID = "id";
        public final static String COLUMN_BOOK_TITLE = "title";
        public final static String COLUMN_BOOK_DESC = "desc";
        public final static String COLUMN_BOOK_ABSOLUTE_PATH = "absolutePath";
        public final static String COLUMN_BOOK_AUTHOR = "author";
        public final static String COLUMN_BOOK_YEAR = "year";
        public final static String COLUMN_BOOK_DURATION = "duration";
    }

    public static final class CatalogEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATALOG).build();

        public final static String TABLE_NAME = "book_catalog";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_BOOK_IMAGE_URL = "imageUrl";
        public final static String COLUMN_BOOK_THUMBNAIL = "thumbnailUrl";
        public final static String COLUMN_BOOK_ID = "id";
        public final static String COLUMN_BOOK_TITLE = "title";
        public final static String COLUMN_BOOK_GENRE = "genre";
        public final static String COLUMN_BOOK_DESC = "desc";
        public final static String COLUMN_BOOK_DOWNLOAD_URL = "downloadUrl";
        public final static String COLUMN_BOOK_AUTHOR = "author";
        public final static String COLUMN_BOOK_YEAR = "year";
        public final static String COLUMN_BOOK_DURATION = "duration";
    }

    public static final class FavouriteEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).build();

        public final static String TABLE_NAME = "favouriteBooks";
        public final static String _ID = BaseColumns._ID;
    }
}
