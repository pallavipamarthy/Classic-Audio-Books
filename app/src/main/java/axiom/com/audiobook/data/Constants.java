package axiom.com.audiobook.data;

import java.util.HashMap;

public class Constants {
    public static final String BOOK_QUERY = "https://librivox.org/api/feed/audiobooks";
    public static final String BOOKD_ID = "?id=";
    public static final String QUERY_END_STRING = "?format=json";
    public static final String EXTENDED_QUERY_END_STRING = "&format=json&extended=1";

    public static final String BOOK_ROMANCE_GENRE_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/romance?format=json";
    public static final String BOOK_DRAMA_GENRE_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/drama?format=json";
    public static final String BOOK_COMEDY_GENRE_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/comedy?format=json";

    public static final String ACTION_DOWNLOAD_PROGRESS = "axiom.com.audiobook.ACTION_DOWNLOAD_PROGRESS";
    public static final String ACTION_CATALOG_DOWNLOAD_FINISHED = "axiom.com.audiobook.ACTION_CATALOG_DOWNLOAD_FINISHED";
    public static final String ACTION_NO_BOOKS_FOUND = "axiom.com.audiobook.ACTION_NO_BOOKS_FOUND";

    public static final String ROMANCE_GENRE = "romance";
    public static final String COMEDY_GENRE = "comedy";
    public static final String DRAMA_GENRE = "drama";

    public static final int DEVICE_TYPE_PHONE = 1;
    public static final int DEVICE_TYPE_TABLET = 2;
    public static final int DEVICE_TYPE_LARGE_TABLET = 3;

    public static final String ACTION_PLAY_CHAPTER = "axiom.com.audiobook.ACTION_PLAY_CHAPTER";
    public static final String ACTION_PAUSE_CHAPTER = "axiom.com.audiobook.ACTION_PAUSE_CHAPTER";
    public static final String ACTION_PROGRESS = "axiom.com.audiobook.ACTION_PROGRESS";
    public static final String ACTION_REWIND_30 = "axiom.com.audiobook.ACTION_REWIND_30";
    public static final String ACTION_FF_30 = "axiom.com.audiobook.ACTION_FF_30";
    public static final String ACTION_STOP_CHAPTER = "axiom.com.audiobook.ACTION_STOP_CHAPTER";
    public static final String ACTION_BEGIN_NEW_CHAPTER = "axiom.com.audiobook.ACTION_BEGIN_NEW_CHAPTER";

    public static final String EXTRA_CHAPTER_PATH = "extra_chapter_path";
    public static final String EXTRA_PLAYBACK_POSITION = "extra_playback_position";

    public static final String GENERAL_FICITON_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/general%20fiction?format=json";
    public static final String BOOK_CRIME_MYSTERY_GENRE_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/crime%20%26%20mystery%20fiction?format=json";
    public static final String FANTASY_FICTION_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/fantasy%20fiction?format=json";
    public static final String HORROR_SUPERNATURAL_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/horror%20%26%20supernatural%20fiction?format=json";
    public static final String ACTION_ADVENTURE_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/action%20%26%20adventure%20fiction?format=json";
    public static final String DETECTIVE_FICTION_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/detective%20fiction?format=json";
    public static final String MYTHS_LEGENDS_FAIRY_TALES_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/myths%2c%20legends%20%26%20fairy%20tales?format=json";
    public static final String SCIENCE_FICTION_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/science%20fiction?format=json";
    public static final String HUMOROUS_FICTION_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/humorous%20fiction?format=json";
    public static final String SHORT_STORIES_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/short%20stories?format=json";
    public static final String SATIRE_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/satire?format=json";
    public static final String SUSPENSE_ESPIONAGE_POLITICAL_THRILLERS_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/suspense%2c%20espionage%2c%20political%20%26%20thrillers?format=json";
    public static final String LITERARY_COLLECTIONS_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/literary%20collections?format=json";
    public static final String PSYCHOLOGY_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/psychology?format=json";
    public static final String TRUE_CRIME_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/true%20crime?format=json";
    public static final String TRAVEL_GEOGRAPHY_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/travel%20%26%20geography?format=json";
    public static final String HISTORICAL_FICTION =
            "https://librivox.org/api/feed/audiobooks/genre/historical%20fiction?format=json";
    public static final String CULTURE_AND_HERITAGE_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/culture%20%26%20heritage?format=json";
    public static final String PLAYS_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/plays?format=json";
    public static final String POETRY_QUERY =
            "https://librivox.org/api/feed/audiobooks/genre/poetry?format=json";

    static HashMap<Integer,String> mHashMap;
    static{
        mHashMap = new HashMap<>();
        mHashMap.put(0,Constants.GENERAL_FICITON_QUERY);
        mHashMap.put(1,Constants.BOOK_CRIME_MYSTERY_GENRE_QUERY);
        mHashMap.put(2,Constants.HORROR_SUPERNATURAL_QUERY);
        mHashMap.put(3,Constants.FANTASY_FICTION_QUERY);
        mHashMap.put(4,Constants.ACTION_ADVENTURE_QUERY);
        mHashMap.put(5,Constants.DETECTIVE_FICTION_QUERY);
        mHashMap.put(6,Constants.MYTHS_LEGENDS_FAIRY_TALES_QUERY);
        mHashMap.put(7,Constants.SCIENCE_FICTION_QUERY);
        mHashMap.put(8,Constants.HUMOROUS_FICTION_QUERY);
        mHashMap.put(9,Constants.SHORT_STORIES_QUERY);
        mHashMap.put(10,Constants.SATIRE_QUERY);
        mHashMap.put(11,Constants.SUSPENSE_ESPIONAGE_POLITICAL_THRILLERS_QUERY);
        mHashMap.put(12,Constants.LITERARY_COLLECTIONS_QUERY);
        mHashMap.put(13,Constants.PSYCHOLOGY_QUERY);
        mHashMap.put(14,Constants.TRUE_CRIME_QUERY);
        mHashMap.put(15,Constants.TRAVEL_GEOGRAPHY_QUERY);
        mHashMap.put(16,Constants.HISTORICAL_FICTION);
        mHashMap.put(17,Constants.CULTURE_AND_HERITAGE_QUERY);
        mHashMap.put(18,Constants.PLAYS_QUERY);
        mHashMap.put(19,Constants.POETRY_QUERY);
    }

    public static String getUrlFromMap(int index){
        String urlString="";
        if(mHashMap.containsKey(index)){
            urlString = mHashMap.get(index);
        }
        return urlString;
    }

    public enum PlaybackStatus {
        PLAYING,
        PAUSED
    }

    public static final String ACTION_PLAY = "axiom.com.audiobook.ACTION_PLAY";
    public static final String ACTION_PAUSE = "axiom.com.audiobook.ACTION_PAUSE";
    public static final String ACTION_STOP = "axiom.com.audiobook.ACTION_STOP";
    public static final String ACTION_PLAYBACK_STOPPED = "axiom.com.audiobook.ACTION_PLAYBACK_STOPPED";
    public static final String ACTION_DOWNLOAD_COMPLETED = "axiom.com.audiobook.ACTION_DOWNLOAD_COMPLETED";

    public static final String EXTRA_BOOK_ID = "book_id";
}

