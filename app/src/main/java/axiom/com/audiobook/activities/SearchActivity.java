package axiom.com.audiobook.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.silvestrpredko.dotprogressbar.DotProgressBar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import axiom.com.audiobook.BooksLoader;
import axiom.com.audiobook.R;
import axiom.com.audiobook.adapter.BooksAdapter;
import axiom.com.audiobook.data.Book;
import axiom.com.audiobook.data.Constants;
import axiom.com.audiobook.data.NetworkUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends MyBottomNavigationActivity implements LoaderManager.LoaderCallbacks<List<Book>> {
    @BindView(R.id.search_edit_text)
    EditText mSearchEditText;
    @BindView(R.id.search_button_image)
    ImageView mSearchImageView;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    BooksAdapter mBookAdapter;
    ArrayList<Book> mCurrentBookList;
    private static final int BOOK_LOADER = 1;
    String mSpinnerSelection = "";
    String mSearchString = "";
    @BindView(R.id.empty_text_view)
    TextView mEmptyTextView;
    @BindView(R.id.progress_bar_view)
    DotProgressBar mProgressBar;
    SearchResultReceiver mSearchResultReceiver;
    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        ButterKnife.bind(this);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        getSupportActionBar().setLogo(R.drawable.gap_between_title_and_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Spinner spinner = findViewById(R.id.search_selection_spinner);
        ArrayAdapter adapter = ArrayAdapter.
                createFromResource(this, R.array.book_selection_spinner, R.layout.spinner_list_item);
        adapter.setDropDownViewResource(R.layout.spinner_list_item);
        spinner.setAdapter(adapter);
        mSpinnerSelection = (String) spinner.getItemAtPosition(0).toString().toLowerCase();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSpinnerSelection = adapterView.getItemAtPosition(i).toString().toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mCurrentBookList = new ArrayList<>();
        mBookAdapter = new BooksAdapter(SearchActivity.this, mCurrentBookList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        mRecyclerView.setAdapter(mBookAdapter);

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        mSearchImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                performSearch();
            }
        });
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        String query = createQuery();
        return new BooksLoader(this, query);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> booksList) {
        mCurrentBookList = (ArrayList<Book>) booksList;
        mProgressBar.setVisibility(View.GONE);
        if (mCurrentBookList != null && !mCurrentBookList.isEmpty()) {
            mEmptyTextView.setVisibility(View.INVISIBLE);
            mBookAdapter.setData(mCurrentBookList);
        } else {
            mEmptyTextView.setVisibility(View.VISIBLE);
        }

        getSupportLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        mCurrentBookList = new ArrayList<>();
    }

    public String createQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.BOOK_QUERY);
        sb.append("/");
        sb.append(mSpinnerSelection);
        sb.append("/%5E");
        try {
            sb.append(URLEncoder.encode(mSearchString, "UTF-8").replaceAll("\\+", "%20"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sb.append(Constants.QUERY_END_STRING);
        return sb.toString();
    }

    private void performSearch() {
        mEmptyTextView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        mSearchString = "";
        mSearchString = mSearchEditText.getText().toString();
        if ((mSearchString.equals("") || mSearchString == null)) {
            Toast.makeText(SearchActivity.this, getString(R.string.empty_search_toast),
                    Toast.LENGTH_LONG).show();
        } else {
            getSupportLoaderManager().restartLoader(BOOK_LOADER, null, SearchActivity.this);
        }
    }

    private class SearchResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.ACTION_NO_BOOKS_FOUND)) {
                mProgressBar.setVisibility(View.INVISIBLE);
                mEmptyTextView.setText(getString(R.string.no_book_results_found));
                mEmptyTextView.setVisibility(View.VISIBLE);
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (!NetworkUtils.isNetworkConnected(context)) {
                    Intent noNetworkIntent = new Intent(context, NetworkConnectionActivity.class);
                    startActivity(noNetworkIntent);
                } else {
                    mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                performSearch();
                                return true;
                            }
                            return false;
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearchResultReceiver = new SearchResultReceiver();
        IntentFilter exceptionfilter = new IntentFilter(Constants.ACTION_NO_BOOKS_FOUND);
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mSearchResultReceiver, exceptionfilter);
        registerReceiver(mSearchResultReceiver, networkFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mSearchResultReceiver);
    }
}
