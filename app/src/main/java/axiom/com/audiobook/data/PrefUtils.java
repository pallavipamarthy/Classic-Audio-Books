package axiom.com.audiobook.data;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {
    public static final String GENRE_PREF = "genre_pref";

    public static void setValueForGenre(Context context, String genre, boolean b) {
        SharedPreferences.Editor editor = context.getSharedPreferences(GENRE_PREF, Context.MODE_PRIVATE).edit();
        editor.putBoolean(genre, b);
        editor.commit();
    }

    public static boolean retrieveValueForGenre(Context context, String genre) {
        SharedPreferences prefs = context.getSharedPreferences(GENRE_PREF, Context.MODE_PRIVATE);
        return prefs.getBoolean(genre, false);
    }

    public static boolean checkAllCategoriesInDb(Context context) {
        return (retrieveValueForGenre(context, Constants.ROMANCE_GENRE)
                && retrieveValueForGenre(context, Constants.COMEDY_GENRE) &&
                retrieveValueForGenre(context, Constants.DRAMA_GENRE));
    }
}
