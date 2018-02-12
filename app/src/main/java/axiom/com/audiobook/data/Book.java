package axiom.com.audiobook.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Book implements Parcelable {
    private String mImageUrl;
    private String mThumbNailImageUrl;
    private String mBookId;
    private String mTitle;
    private String mDescription;
    private String mZipFileUrlString;
    private String mAuthorName;
    private String mCopyrightYear;
    private String mTotalTime;
    private String mAbsolutePath;
    private ArrayList<Chapter> mChapterList;

    public Book(String imageUrl,
                String thumbNailImageUrl,
                String bookId,
                String title,
                String desc,
                String zipFileUrlString,
                String authorName,
                String copyrightYear,
                String totalTime,
                ArrayList<Chapter> chapterList) {
        mImageUrl = imageUrl;
        mThumbNailImageUrl = thumbNailImageUrl;
        mBookId = bookId;
        mTitle = title;
        mDescription = desc;
        mZipFileUrlString = zipFileUrlString;
        mAuthorName = authorName;
        mCopyrightYear = copyrightYear;
        mTotalTime = totalTime;
        mChapterList = chapterList;
        mAbsolutePath = null;
    }

    private Book(Parcel in) {
        mImageUrl = in.readString();
        mThumbNailImageUrl = in.readString();
        mBookId = in.readString();
        mTitle = in.readString();
        mDescription = in.readString();
        mZipFileUrlString = in.readString();
        mAuthorName = in.readString();
        mCopyrightYear = in.readString();
        mTotalTime = in.readString();
        mChapterList = new ArrayList<Chapter>();
        in.readTypedList(mChapterList, Chapter.CREATOR);
        mAbsolutePath = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getThumbNailUrl() {
        return mThumbNailImageUrl;
    }

    public String getBookId() {
        return mBookId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getZipFileUrlString() {
        return mZipFileUrlString;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public String getCopyrightYear() {
        return mCopyrightYear;
    }

    public String getTotalTime() {
        return mTotalTime;
    }

    public ArrayList<Chapter> getChapterList() {
        return mChapterList;
    }

    public String getAbsolutePath(){return mAbsolutePath;}

    public void setAbsolutePath(String absolutePath){
        mAbsolutePath = absolutePath;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mImageUrl);
        parcel.writeString(mThumbNailImageUrl);
        parcel.writeString(mBookId);
        parcel.writeString(mTitle);
        parcel.writeString(mDescription);
        parcel.writeString(mZipFileUrlString);
        parcel.writeString(mAuthorName);
        parcel.writeString(mCopyrightYear);
        parcel.writeString(mTotalTime);
        parcel.writeTypedList(mChapterList);
        parcel.writeString(mAbsolutePath);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel parcel) {
            return new Book(parcel);
        }

        @Override
        public Book[] newArray(int i) {
            return new Book[i];
        }

    };
}

