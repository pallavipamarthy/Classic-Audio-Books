package axiom.com.audiobook.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import axiom.com.audiobook.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryListActivity extends MyBottomNavigationActivity {
    @BindView(R.id.list_view)
    ListView mCategoryListView;
    @BindView(R.id.navigation)
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_list_layout);
        ButterKnife.bind(this);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setTitle(getString(R.string.more_categories_text));
        getSupportActionBar().setLogo(R.drawable.gap_between_title_and_icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String[] categoryArray = getResources().getStringArray(R.array.more_categories_list);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryArray);
        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent listItemClick = new Intent(CategoryListActivity.this, GenreCatalogActivity.class);
                listItemClick.putExtra(getString(R.string.genre_extra_text), (String) parent.getItemAtPosition(position));
                listItemClick.putExtra(getString(R.string.is_network_genre_extra), true);
                listItemClick.putExtra(getString(R.string.selected_list_item_text), position + 4);
                startActivity(listItemClick);
            }
        });
        mCategoryListView.setAdapter(adapter);
    }
}
