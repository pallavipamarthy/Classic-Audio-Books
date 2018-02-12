package axiom.com.audiobook.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import axiom.com.audiobook.BlurBuilder;
import axiom.com.audiobook.DownloadIntentService;
import axiom.com.audiobook.ObservableScrollView;
import axiom.com.audiobook.R;
import axiom.com.audiobook.data.Book;
import axiom.com.audiobook.data.BookContract;
import axiom.com.audiobook.data.BookContract.BookEntry;
import axiom.com.audiobook.data.BookDbHelper;
import axiom.com.audiobook.data.Constants;
import axiom.com.audiobook.data.NetworkUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BookDetailActivity extends AppCompatActivity {
    @BindView(R.id.detail_book_image_view)
    ImageView mDetailBookImageView;
    @BindView(R.id.copyright_year_textview)
    TextView mCopyrightYearTextView;
    @BindView(R.id.detail_author_name_view)
    TextView mDetailAuthorNameView;
    @BindView(R.id.total_time)
    TextView mDurationTextView;
    @BindView(R.id.description_textview)
    TextView mDescriptionTextView;
    @BindView(R.id.download_image_button)
    Button mDownloadButton;
    Book mCurrentBook;
    private NetworkInfoReceiver mNetworkInfoReceiver;
    BookDbHelper mDbHelper;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    Snackbar mSnackBar;
    @BindView(R.id.detail_thumbnail_image_view)
    ImageView mThumbnailImageView;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.scrollview)
    ObservableScrollView mObservableScrollView;
    private static final float PARALLAX_FACTOR = 1.25f;
    private int mScrollY;
    @BindView(R.id.photo_container)
    View mPhotoContainerView;
    @BindView(R.id.wishlist_image_button)
    Button mFavouriteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        ButterKnife.bind(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mCurrentBook = getIntent().getParcelableExtra("book_obj");

        getSupportActionBar().setLogo(R.drawable.gap_between_title_and_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");


        mObservableScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
            @Override
            public void onScrollChanged() {
                mScrollY = mObservableScrollView.getScrollY();
                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
            }
        });

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(mCurrentBook.getTitle());
                    mThumbnailImageView.setVisibility(View.INVISIBLE);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    mThumbnailImageView.setVisibility(View.VISIBLE);
                    isShow = false;
                }
            }
        });

        setBackgroundImage();
        String bookId = mCurrentBook.getBookId();
        mDetailAuthorNameView.setText(mCurrentBook.getAuthorName());
        mCopyrightYearTextView.setText(mCurrentBook.getCopyrightYear());
        String desc = android.text.Html.fromHtml(mCurrentBook.getDescription()).toString();
        mDescriptionTextView.setText(desc);
        mDurationTextView.setText(mCurrentBook.getTotalTime());

        boolean isInDownloadDb = checkBookInDownloadDb(bookId);
        if (isInDownloadDb) {
            setListenButton();
        } else {
            setDownloadButton();
        }

        boolean isInFavDb = checkBookInFavDb(bookId);

        if (isInDownloadDb) {
            setDeleteButton();
        } else if (isInFavDb) {
            setBookInWishlistButton();
        } else {
            setAddToWishListButton();
        }

        if (!NetworkUtils.isNetworkConnected(BookDetailActivity.this)) {
            showSnackBar();
        }
        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkUtils.isNetworkConnected(BookDetailActivity.this)) {
                    showSnackBar();
                } else {
                    if (mDownloadButton.getTag().toString() != null && mDownloadButton.getTag().toString().equals(getString(R.string.listen_button_tag))) {
                        Intent intent = new Intent(BookDetailActivity.this, AudioPlayerActivity.class);
                        intent.putExtra("book_obj", mCurrentBook);
                        startActivity(intent);
                    } else if (mDownloadButton.getTag().toString() != null && mDownloadButton.getTag().toString().equals(getString(R.string.download_button_tag))) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(BookDetailActivity.this, DownloadIntentService.class);
                        intent.putExtra("book_obj", mCurrentBook);
                        startService(intent);
                    }
                }
            }
        });

        mFavouriteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mFavouriteButton.getTag() != null &&
                        mFavouriteButton.getTag().toString().equals(getString(R.string.add_to_wishlist_tag))) {
                    addBook(mCurrentBook);
                    setBookInWishlistButton();
                    Toast.makeText(BookDetailActivity.this, getString(R.string.added_to_wishlist_toast), Toast.LENGTH_SHORT).show();
                } else if (mFavouriteButton.getTag() != null &&
                        mFavouriteButton.getTag().toString().equals(getString(R.string.in_wishlist_tag))) {
                    deleteBookFromFav(mCurrentBook);
                    setAddToWishListButton();
                    Toast.makeText(BookDetailActivity.this, getString(R.string.removed_from_wishlist_toast), Toast.LENGTH_SHORT).show();
                } else {
                    showDeleteAlertDialog();
                }
            }
        });
        mNetworkInfoReceiver = new NetworkInfoReceiver();
        IntentFilter downloadProgressFilter = new IntentFilter(Constants.ACTION_DOWNLOAD_PROGRESS);
        downloadProgressFilter.addAction(Constants.ACTION_DOWNLOAD_COMPLETED);
        registerReceiver(mNetworkInfoReceiver, downloadProgressFilter);

    }

    private class NetworkInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_DOWNLOAD_PROGRESS.equals(intent.getAction()) &&
                    mCurrentBook.getBookId().equals(intent.getStringExtra(Constants.EXTRA_BOOK_ID))) {
                int progressExtra = intent.getIntExtra(getString(R.string.progress_extra_text), 0);
                if (mDownloadButton.getTag().toString() != null &&
                        !mDownloadButton.getTag().toString().equals(getString(R.string.downloading_button_tag))) {
                    setDownloadingButton();
                }
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(progressExtra);

            } else if (intent.getAction().equals(Constants.ACTION_DOWNLOAD_COMPLETED)) {
                mProgressBar.setVisibility(View.INVISIBLE);
                mProgressBar.setProgress(0);
                setListenButton();
                setDeleteButton();
                deleteBookFromFav(mCurrentBook);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkInfoReceiver);
    }

    public void showSnackBar() {
        RelativeLayout relativeLayout = findViewById(R.id.relative_layout);
        mSnackBar = Snackbar
                .make(relativeLayout, getString(R.string.snackbar_text), Snackbar.LENGTH_LONG);
        mSnackBar.show();
    }

    private void setBackgroundImage() {
        Picasso.with(this).load(mCurrentBook.getImageUrl()).placeholder(R.mipmap.default_placeholder)
                .into(mThumbnailImageView);
        Picasso.with(this).load(mCurrentBook.getImageUrl()).placeholder(R.mipmap.default_placeholder)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Bitmap blurredBitmap = BlurBuilder.blur(BookDetailActivity.this, bitmap);
                        mDetailBookImageView.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
    }

    public void addBook(Book currentBook) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookContract.CatalogEntry.COLUMN_BOOK_IMAGE_URL, currentBook.getImageUrl());
        contentValues.put(BookContract.CatalogEntry.COLUMN_BOOK_THUMBNAIL, currentBook.getThumbNailUrl());
        contentValues.put(BookContract.CatalogEntry.COLUMN_BOOK_ID, currentBook.getBookId());
        contentValues.put(BookContract.CatalogEntry.COLUMN_BOOK_TITLE, currentBook.getTitle());
        contentValues.put(BookContract.CatalogEntry.COLUMN_BOOK_DESC, currentBook.getDescription());
        contentValues.put(BookContract.CatalogEntry.COLUMN_BOOK_DOWNLOAD_URL, currentBook.getZipFileUrlString());
        contentValues.put(BookContract.CatalogEntry.COLUMN_BOOK_AUTHOR, currentBook.getAuthorName());
        contentValues.put(BookContract.CatalogEntry.COLUMN_BOOK_YEAR, currentBook.getCopyrightYear());
        contentValues.put(BookContract.CatalogEntry.COLUMN_BOOK_DURATION, currentBook.getTotalTime());
        getContentResolver().insert(BookContract.FavouriteEntry.CONTENT_URI, contentValues);
    }

    private boolean checkBookInDownloadDb(String bookId) {
        mDbHelper = new BookDbHelper(this);
        String[] projection = {BookEntry.COLUMN_BOOK_ID};
        String selection = BookEntry.COLUMN_BOOK_ID + "=?";
        String[] selectionArgs = {bookId};
        String downloadedBookId;
        Cursor cursor = getContentResolver().query(BookEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        while (cursor.moveToNext()) {
            downloadedBookId = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_ID));
            if (downloadedBookId.equals(bookId)) {
                cursor.close();
                return true;
            }
        }
        cursor.close();
        mDbHelper.close();
        return false;
    }

    private boolean checkBookInFavDb(String bookId) {
        String favouriteBookId;
        mDbHelper = new BookDbHelper(this);
        String[] projection = {BookEntry.COLUMN_BOOK_ID};
        String selection = BookEntry.COLUMN_BOOK_ID + "=?";
        String[] selectionArgs = {bookId};
        Cursor cursor = getContentResolver().query(BookContract.FavouriteEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        while (cursor.moveToNext()) {
            favouriteBookId = cursor.getString(cursor.getColumnIndexOrThrow(BookContract.CatalogEntry.COLUMN_BOOK_ID));
            if (favouriteBookId.equals(bookId)) {
                cursor.close();
                return true;
            }
        }
        cursor.close();
        mDbHelper.close();
        return false;
    }

    private void deleteBookFromFav(Book book) {
        mDbHelper = new BookDbHelper(this);
        String selection = BookEntry.COLUMN_BOOK_ID + "=?";
        String[] selectionArgs = {book.getBookId()};
        getContentResolver().delete(BookContract.FavouriteEntry.CONTENT_URI, selection, selectionArgs);
        mDbHelper.close();
    }

    private void deleteBookFromDownloadDb(Book book) {
        mDbHelper = new BookDbHelper(this);
        String selection = BookEntry.COLUMN_BOOK_ID + "=?";
        String[] selectionArgs = {book.getBookId()};
        getContentResolver().delete(BookContract.BookEntry.CONTENT_URI, selection, selectionArgs);
        mDbHelper.close();
    }

    private void showDeleteAlertDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(getString(R.string.delete_book_dialog_title))
                .setMessage(getString(R.string.delete_book_dialog_message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteBookFile(mCurrentBook);
                        deleteBookFromDownloadDb(mCurrentBook);
                        setAddToWishListButton();
                        setDownloadButton();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteBookFile(Book book) {
        String absolutePath = "";
        mDbHelper = new BookDbHelper(this);
        String[] projection = {BookEntry.COLUMN_BOOK_ABSOLUTE_PATH};
        String selection = BookEntry.COLUMN_BOOK_ID + "=?";
        String[] selectionArgs = {book.getBookId()};
        Cursor cursor = getContentResolver().query(BookContract.BookEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        while (cursor.moveToNext()) {
            absolutePath = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_ABSOLUTE_PATH));
        }
        cursor.close();
        File dir = new File(absolutePath);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String s : children) {
                new File(dir, s).delete();
            }
        }
        dir.delete();
        mDbHelper.close();
    }

    private void setAddToWishListButton() {
        mFavouriteButton.setText(getString(R.string.add_to_wishlist_text));
        mFavouriteButton.setTag(getString(R.string.add_to_wishlist_tag));
        mFavouriteButton.setBackground(ContextCompat.getDrawable(BookDetailActivity.this, R.drawable.transparent_button));
        mFavouriteButton.setTextColor(ContextCompat.getColor(BookDetailActivity.this, R.color.colorPrimary));
    }

    private void setBookInWishlistButton() {
        mFavouriteButton.setText(getString(R.string.in_wishlist_text));
        mFavouriteButton.setTag(getString(R.string.in_wishlist_tag));
        mFavouriteButton.setBackgroundColor(ContextCompat.getColor(BookDetailActivity.this, R.color.colorPrimary));
        mFavouriteButton.setTextColor(ContextCompat.getColor(BookDetailActivity.this, android.R.color.white));
    }

    private void setDownloadButton() {
        mDownloadButton.setTag(getString(R.string.download_button_tag));
        mDownloadButton.setText(getString(R.string.download_button_text));
    }

    private void setDownloadingButton() {
        mDownloadButton.setTag(getString(R.string.downloading_button_tag));
        mDownloadButton.setText(getString(R.string.downloading_button_text));
    }

    private void setListenButton() {
        mCurrentBook.setAbsolutePath(getAbsolutePathForChapters());
        mDownloadButton.setTag(getString(R.string.listen_button_tag));
        mDownloadButton.setText(getString(R.string.listen_button_tag));
    }

    private void setDeleteButton() {
        mFavouriteButton.setTag(getString(R.string.delete_button_tag));
        mFavouriteButton.setText(getString(R.string.delete_button_text));
    }

    private String getAbsolutePathForChapters() {
        String absolutePath = "";
        mDbHelper = new BookDbHelper(this);
        String[] projection = {BookEntry.COLUMN_BOOK_ABSOLUTE_PATH};
        String selection = BookEntry.COLUMN_BOOK_ID + "=?";
        String[] selectionArgs = {mCurrentBook.getBookId()};
        Cursor cursor = getContentResolver().query(BookContract.BookEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        while (cursor.moveToNext()) {
            absolutePath = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry.COLUMN_BOOK_ABSOLUTE_PATH));
        }
        cursor.close();
        mDbHelper.close();
        return absolutePath;
    }
}
