package axiom.com.audiobook.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import axiom.com.audiobook.CatalogCursorLoader;
import axiom.com.audiobook.R;
import axiom.com.audiobook.adapter.BooksAdapter;
import axiom.com.audiobook.data.Book;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadsActivity extends MyBottomNavigationActivity implements LoaderManager.LoaderCallbacks<List<Book>> {
    @BindView(R.id.recyclerView)
    RecyclerView mDownloadRecyclerView;
    BooksAdapter mBookAdapter;
    @BindView(R.id.empty_text_view)
    TextView mEmptyView;
    private static final int BOOK_LOADER = 1;
    ArrayList<Book> mBookList;
    boolean isFavoriteActivity;
    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);
        ButterKnife.bind(this);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mBookList = new ArrayList<>();

        String activityNameExtra = getIntent().getStringExtra(getString(R.string.activity_name_extra_text));
        if (activityNameExtra != null && activityNameExtra.equals(getString(R.string.favourites_text))) {
            isFavoriteActivity = true;
            setTitle(getString(R.string.favourites_text));
            mEmptyView.setText(getResources().getString(R.string.empty_favorites_text));
        } else if (activityNameExtra == null) {
            setTitle(getString(R.string.my_downloads_text));
            mEmptyView.setText(getResources().getString(R.string.empty_downloads_text));
        }

        getSupportActionBar().setLogo(R.drawable.gap_between_title_and_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportLoaderManager().initLoader(BOOK_LOADER, null, this);
        mBookAdapter = new BooksAdapter(DownloadsActivity.this, mBookList);
        mDownloadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDownloadRecyclerView.setAdapter(mBookAdapter);
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        if (isFavoriteActivity) {
            return new CatalogCursorLoader(this, getString(R.string.favourites_text));
        } else {
            return new CatalogCursorLoader(this, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> booksList) {
        mBookList = (ArrayList<Book>) booksList;
        if (mBookList != null) {
            if (!mBookList.isEmpty()) {
                mEmptyView.setVisibility(View.INVISIBLE);
            }
            mBookAdapter.setData(mBookList);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {

    }


}
