package axiom.com.audiobook.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.silvestrpredko.dotprogressbar.DotProgressBar;

import java.util.ArrayList;
import java.util.List;

import axiom.com.audiobook.CatalogCursorLoader;
import axiom.com.audiobook.CatalogFetchIntentService;
import axiom.com.audiobook.R;
import axiom.com.audiobook.adapter.CollectionBookAdapter;
import axiom.com.audiobook.data.Book;
import axiom.com.audiobook.data.Constants;
import axiom.com.audiobook.data.NetworkUtils;
import axiom.com.audiobook.data.PrefUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends MyBottomNavigationActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    @BindView(R.id.recyclerView1)
    RecyclerView mRecyclerView1;
    @BindView(R.id.recyclerView2)
    RecyclerView mRecyclerView2;
    @BindView(R.id.recyclerView3)
    RecyclerView mRecyclerView3;
    ArrayList<Book> mRomanceBookList;
    ArrayList<Book> mDramaBookList;
    ArrayList<Book> mComedyBookList;
    private static final int ROMANCE_BOOK_LOADER = 1;
    private static final int COMEDY_BOOK_LOADER = 2;
    private static final int DRAMA_BOOK_LOADER = 3;
    CollectionBookAdapter mRomanceBookAdapter;
    CollectionBookAdapter mComedyBookAdapter;
    CollectionBookAdapter mDramaBookAdapter;
    CatalogReceiver mCatalogReceiver;
    LoaderManager.LoaderCallbacks<List<Book>> mLoaderCallback;
    @BindView(R.id.progress_bar1)
    DotProgressBar mProgressBar1;
    @BindView(R.id.progress_bar2)
    DotProgressBar mProgressBar2;
    @BindView(R.id.progress_bar3)
    DotProgressBar mProgressBar3;

    @BindView(R.id.romance_on_click_view)
    TextView mRomanceClickView;
    @BindView(R.id.drama_on_click_view)
    TextView mDramaClickView;
    @BindView(R.id.comedy_on_click_view)
    TextView mComedyClickView;
    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.crime_mystery_on_click_view)
    CardView mCrimeMysteryClickView;
    @BindView(R.id.horror_supernatural_click_view)
    CardView mHorrorSupernaturalClickView;
    @BindView(R.id.fantasy_on_click_view)
    CardView mFantasyClickView;
    @BindView(R.id.general_fiction_on_click_view)
    CardView mGeneralFictionClickView;
    Snackbar mSnackBar;

    Loader<List<Book>> mRomanceCursorLoader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        getSupportActionBar().setLogo(R.drawable.gap_between_title_and_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mLoaderCallback = this;

        if (mComedyBookList == null) {
            mComedyBookList = new ArrayList<>();
        }
        if (mRomanceBookList == null) {
            mRomanceBookList = new ArrayList<>();
        }
        if (mDramaBookList == null) {
            mDramaBookList = new ArrayList<>();
        }

        mRecyclerView1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView3.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mRomanceBookAdapter = new CollectionBookAdapter(MainActivity.this, mRomanceBookList, R.layout.horizontal_scrollview_grid_item);
        mRomanceBookAdapter.setData(mRomanceBookList);
        mRecyclerView1.setAdapter(mRomanceBookAdapter);

        mComedyBookAdapter = new CollectionBookAdapter(MainActivity.this, mComedyBookList, R.layout.horizontal_scrollview_grid_item);
        mComedyBookAdapter.setData(mComedyBookList);
        mRecyclerView2.setAdapter(mComedyBookAdapter);

        mDramaBookAdapter = new CollectionBookAdapter(MainActivity.this, mDramaBookList, R.layout.horizontal_scrollview_grid_item);
        mDramaBookAdapter.setData(mDramaBookList);
        mRecyclerView3.setAdapter(mDramaBookAdapter);

        mRomanceClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGenreSelect(Constants.ROMANCE_GENRE);
            }
        });
        mComedyClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGenreSelect(Constants.COMEDY_GENRE);
            }
        });
        mDramaClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGenreSelect(Constants.DRAMA_GENRE);
            }
        });

        mGeneralFictionClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GenreCatalogActivity.class);
                intent.putExtra(getString(R.string.genre_extra_text), getString(R.string.general_fiction_text));
                intent.putExtra(getString(R.string.is_network_genre_extra), true);
                intent.putExtra(getString(R.string.selected_list_item_text), 0);
                startActivity(intent);
            }
        });

        mCrimeMysteryClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GenreCatalogActivity.class);
                intent.putExtra(getString(R.string.genre_extra_text), getString(R.string.crime_mystery_text));
                intent.putExtra(getString(R.string.is_network_genre_extra), true);
                intent.putExtra(getString(R.string.selected_list_item_text), 1);
                startActivity(intent);
            }
        });
        mHorrorSupernaturalClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GenreCatalogActivity.class);
                intent.putExtra(getString(R.string.genre_extra_text), getString(R.string.horror_supernatural_text));
                intent.putExtra(getString(R.string.is_network_genre_extra), true);
                intent.putExtra(getString(R.string.selected_list_item_text), 2);
                startActivity(intent);
            }
        });
        mFantasyClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GenreCatalogActivity.class);
                intent.putExtra(getString(R.string.genre_extra_text), getString(R.string.fantasy_text));
                intent.putExtra(getString(R.string.is_network_genre_extra), true);
                intent.putExtra(getString(R.string.selected_list_item_text), 3);
                startActivity(intent);
            }
        });
        RelativeLayout relativeLayout = findViewById(R.id.relative_layout);
        mSnackBar = Snackbar
                .make(relativeLayout, getString(R.string.snackbar_text), Snackbar.LENGTH_SHORT);

    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        if (i == ROMANCE_BOOK_LOADER) {
            mProgressBar1.setVisibility(View.VISIBLE);
            if (mRomanceCursorLoader == null) {
                mRomanceCursorLoader = new CatalogCursorLoader(this, Constants.ROMANCE_GENRE);
            }
            return mRomanceCursorLoader;
        } else if (i == COMEDY_BOOK_LOADER) {
            mProgressBar2.setVisibility(View.VISIBLE);
            return new CatalogCursorLoader(this, Constants.COMEDY_GENRE);
        } else {
            mProgressBar3.setVisibility(View.VISIBLE);
            return new CatalogCursorLoader(this, Constants.DRAMA_GENRE);
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> booksList) {
        int loaderId = loader.getId();
        if (loaderId == ROMANCE_BOOK_LOADER) {
            mRomanceBookList = (ArrayList<Book>) booksList;
            mProgressBar1.setVisibility(View.INVISIBLE);
            mRomanceBookAdapter.setData(mRomanceBookList);
        } else if (loaderId == COMEDY_BOOK_LOADER) {
            mComedyBookList = (ArrayList<Book>) booksList;
            mProgressBar2.setVisibility(View.INVISIBLE);
            mComedyBookAdapter.setData(mComedyBookList);
        } else if (loaderId == DRAMA_BOOK_LOADER) {
            mDramaBookList = (ArrayList<Book>) booksList;
            mProgressBar3.setVisibility(View.INVISIBLE);
            mDramaBookAdapter.setData(mDramaBookList);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {

    }

    public void onGenreSelect(String genre) {
        Intent genreSelectIntent = new Intent(MainActivity.this, GenreCatalogActivity.class);
        genreSelectIntent.putExtra(getString(R.string.genre_extra_text), genre);
        startActivity(genreSelectIntent);
    }

    private class CatalogReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_CATALOG_DOWNLOAD_FINISHED)) {
                String genre = intent.getStringExtra(getString(R.string.genre_extra_text));
                if (genre.equals(Constants.ROMANCE_GENRE)) {
                    getSupportLoaderManager().initLoader(ROMANCE_BOOK_LOADER, null, mLoaderCallback);
                } else if (genre.equals(Constants.COMEDY_GENRE)) {
                    getSupportLoaderManager().initLoader(COMEDY_BOOK_LOADER, null, mLoaderCallback);
                } else if (genre.equals(Constants.DRAMA_GENRE)) {
                    getSupportLoaderManager().initLoader(DRAMA_BOOK_LOADER, null, mLoaderCallback);
                }
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (!NetworkUtils.isNetworkConnected(context)) {
                    if (PrefUtils.checkAllCategoriesInDb(context)) {
                        showSnackBar();
                    } else {
                        Intent noNetworkIntent = new Intent(context, NetworkConnectionActivity.class);
                        startActivity(noNetworkIntent);
                    }
                }
            }
        }
    }

    public void getCatalogBooksFromNetwork() {
        if (!PrefUtils.retrieveValueForGenre(this, Constants.ROMANCE_GENRE)) {
            Intent romanceCatalogService = new Intent(MainActivity.this, CatalogFetchIntentService.class);
            romanceCatalogService.putExtra(getString(R.string.genre_extra_text), Constants.ROMANCE_GENRE);
            romanceCatalogService.putExtra(getString(R.string.is_network_genre_extra), false);
            startService(romanceCatalogService);
        }
        if (!PrefUtils.retrieveValueForGenre(this, Constants.COMEDY_GENRE)) {
            Intent comedyCatalogService = new Intent(MainActivity.this, CatalogFetchIntentService.class);
            comedyCatalogService.putExtra(getString(R.string.genre_extra_text), Constants.COMEDY_GENRE);
            comedyCatalogService.putExtra(getString(R.string.is_network_genre_extra), false);
            startService(comedyCatalogService);
        }
        if (!PrefUtils.retrieveValueForGenre(this, Constants.DRAMA_GENRE)) {
            Intent dramaCatalogService = new Intent(MainActivity.this, CatalogFetchIntentService.class);
            dramaCatalogService.putExtra(getString(R.string.genre_extra_text), Constants.DRAMA_GENRE);
            dramaCatalogService.putExtra(getString(R.string.is_network_genre_extra), false);
            startService(dramaCatalogService);
        }
    }

    private void getCatalogFromDB() {
        if (PrefUtils.retrieveValueForGenre(this, Constants.ROMANCE_GENRE) && mRomanceBookList.size() == 0) {
            getSupportLoaderManager().initLoader(ROMANCE_BOOK_LOADER, null, mLoaderCallback);
        }
        if (PrefUtils.retrieveValueForGenre(this, Constants.COMEDY_GENRE) && mComedyBookList.size() == 0) {
            getSupportLoaderManager().initLoader(COMEDY_BOOK_LOADER, null, mLoaderCallback);
        }
        if (PrefUtils.retrieveValueForGenre(this, Constants.DRAMA_GENRE) && mDramaBookList.size() == 0) {
            getSupportLoaderManager().initLoader(DRAMA_BOOK_LOADER, null, mLoaderCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mCatalogReceiver);
    }

    public void showSnackBar() {
        mSnackBar.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (PrefUtils.checkAllCategoriesInDb(this)) {
            getSupportLoaderManager().initLoader(ROMANCE_BOOK_LOADER, null, mLoaderCallback);
            getSupportLoaderManager().initLoader(COMEDY_BOOK_LOADER, null, mLoaderCallback);
            getSupportLoaderManager().initLoader(DRAMA_BOOK_LOADER, null, mLoaderCallback);
            if (!NetworkUtils.isNetworkConnected(this)) {
                showSnackBar();
            }
        } else {
            if (!NetworkUtils.isNetworkConnected(this)) {
                Intent noNetworkIntent = new Intent(this, NetworkConnectionActivity.class);
                startActivity(noNetworkIntent);
            } else {
                getCatalogFromDB();
                getCatalogBooksFromNetwork();
            }
        }
        mCatalogReceiver = new CatalogReceiver();
        IntentFilter downloadFilter = new IntentFilter(Constants.ACTION_CATALOG_DOWNLOAD_FINISHED);
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mCatalogReceiver, networkFilter);
        registerReceiver(mCatalogReceiver, downloadFilter);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
