package axiom.com.audiobook.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.github.silvestrpredko.dotprogressbar.DotProgressBar;

import java.util.ArrayList;
import java.util.List;

import axiom.com.audiobook.BooksLoader;
import axiom.com.audiobook.CatalogCursorLoader;
import axiom.com.audiobook.R;
import axiom.com.audiobook.adapter.CollectionBookAdapter;
import axiom.com.audiobook.data.Book;
import axiom.com.audiobook.data.Constants;
import axiom.com.audiobook.data.NetworkUtils;
import axiom.com.audiobook.data.PrefUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class GenreCatalogActivity extends MyBottomNavigationActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    @BindView(R.id.genre_catalog_recyclerView)
    RecyclerView mGenreCatalogRecyclerView;
    CollectionBookAdapter mGenreCatalogBookAdapter;
    GenreCatalogReceiver mGenreCatalogReceiver;
    LoaderManager.LoaderCallbacks<List<Book>> mLoaderCallback;
    private static final int STORED_GENRE_BOOK_LOADER = 1;
    private static final int NETWORK_GENRE_BOOK_LOADER = 2;
    ArrayList<Book> mGenreCatalogList;
    @BindView(R.id.genre_catalog_progress_bar)
    DotProgressBar mGenreProgressBar;
    String mGenre = "";
    String title = "";
    int mSelectedListIndex;
    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.genre_catalog_layout);
        ButterKnife.bind(this);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        getSupportActionBar().setLogo(R.drawable.gap_between_title_and_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        boolean isNetworkGenre = getIntent().getBooleanExtra(getString(R.string.is_network_genre_extra), false);
        mGenre = getIntent().getStringExtra(getString(R.string.genre_extra_text));
        mSelectedListIndex = getIntent().getIntExtra(getString(R.string.selected_list_item_text), 0);

        if (mGenre.equals(Constants.ROMANCE_GENRE)) {
            title = getString(R.string.romance_genre_title);
        } else if (mGenre.equals(Constants.COMEDY_GENRE)) {
            title = getString(R.string.comedy_genre_title);
        } else if (mGenre.equals(Constants.DRAMA_GENRE)) {
            title = getString(R.string.drama_genre_title);
        } else {
            title = mGenre;
        }
        setTitle(title);
        mLoaderCallback = this;
        mGenreCatalogList = new ArrayList<>();

        mGenreCatalogReceiver = new GenreCatalogReceiver();
        IntentFilter downloadFilter = new IntentFilter(Constants.ACTION_CATALOG_DOWNLOAD_FINISHED);
        registerReceiver(mGenreCatalogReceiver, downloadFilter);

        if (!isNetworkGenre) {
            if (PrefUtils.retrieveValueForGenre(this, mGenre)) {
                getSupportLoaderManager().initLoader(STORED_GENRE_BOOK_LOADER, null, mLoaderCallback);
            }
        } else {
            if (!NetworkUtils.isNetworkConnected(this)) {
                Intent noNetworkIntent = new Intent(this, NetworkConnectionActivity.class);
                startActivity(noNetworkIntent);
            } else {
                getSupportLoaderManager().initLoader(NETWORK_GENRE_BOOK_LOADER, null, mLoaderCallback);
            }
        }

        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT) {
            int columnCount = getColumnCount(true);
            mGenreCatalogRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));
        } else if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            int columnCount = getColumnCount(false);
            mGenreCatalogRecyclerView.setLayoutManager(new GridLayoutManager(this, columnCount));
        }

        mGenreCatalogBookAdapter = new CollectionBookAdapter(this, mGenreCatalogList, R.layout.catalog_grid_item);
        mGenreCatalogRecyclerView.setAdapter(mGenreCatalogBookAdapter);
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        mGenreProgressBar.setVisibility(View.VISIBLE);
        if (i == STORED_GENRE_BOOK_LOADER) {
            return new CatalogCursorLoader(this, mGenre);
        } else {
            String urlString = Constants.getUrlFromMap(mSelectedListIndex);
            return new BooksLoader(this, urlString);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> booksList) {
        mGenreCatalogList = (ArrayList<Book>) booksList;
        mGenreProgressBar.setVisibility(View.INVISIBLE);
        mGenreCatalogBookAdapter.setData(mGenreCatalogList);
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {

    }

    private class GenreCatalogReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_CATALOG_DOWNLOAD_FINISHED)) {
                getSupportLoaderManager().initLoader(STORED_GENRE_BOOK_LOADER, null, mLoaderCallback);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGenreCatalogReceiver);
    }

    private int getColumnCount(boolean isPotrait) {
        int deviceType = getResources().getInteger(R.integer.deviceType);
        switch (deviceType) {
            //Checking for device type : phone,tablet
            case Constants.DEVICE_TYPE_PHONE:
                if (isPotrait) {
                    // spancount for potrait mode - phone
                    return 3;
                }
            case Constants.DEVICE_TYPE_TABLET:
                if (isPotrait) {
                    return 4;
                } else {
                    return 5;
                }
            case Constants.DEVICE_TYPE_LARGE_TABLET:
                if (isPotrait) {
                    return 4;
                } else {
                    return 5;
                }
            default:
                //settting phone spancount as default value
                return 3;
        }
    }
}
