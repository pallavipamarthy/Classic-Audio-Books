package axiom.com.audiobook.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class ChapterContract {
    public static final String AUTHORITY = "com.example.android.chapterdatabase";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_CHAPTERS = "chapters";

    private ChapterContract() {
    }

    public static final class ChapterEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHAPTERS).build();

        public final static String TABLE_NAME = "chapters";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_BOOK_ID = "book_id";
        public final static String COLUMN_CHAPTER_ID = "id";
        public final static String COLUMN_CHAPTER_TITLE = "title";
        public final static String COLUMN_PLAYTIME = "playtime";
    }
}
