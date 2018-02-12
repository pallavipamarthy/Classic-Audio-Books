package axiom.com.audiobook.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Chapter implements Parcelable {
    private String mBookId;
    private String mChapterId;
    private String mChapterTitle;
    private String mPlaytime;

    public Chapter(String bookId,
                   String chapterId,
                   String chapterTitle,
                   String playTime) {
        mBookId = bookId;
        mChapterId = chapterId;
        mChapterTitle = chapterTitle;
        mPlaytime = playTime;
    }

    private Chapter(Parcel in) {
        mBookId = in.readString();
        mChapterId = in.readString();
        mChapterTitle = in.readString();
        mPlaytime = in.readString();
    }

    public String getBookId() {
        return mBookId;
    }

    public String getChapterId() {
        return mChapterId;
    }

    public String getChapterTitle() {
        return mChapterTitle;
    }

    public String getPlayTime() {
        return mPlaytime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mBookId);
        parcel.writeString(mChapterId);
        parcel.writeString(mChapterTitle);
        parcel.writeString(mPlaytime);
    }

    public static final Parcelable.Creator<Chapter> CREATOR = new Parcelable.Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel parcel) {
            return new Chapter(parcel);
        }

        @Override
        public Chapter[] newArray(int i) {
            return new Chapter[i];
        }

    };
}
