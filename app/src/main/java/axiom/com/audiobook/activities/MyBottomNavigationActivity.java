package axiom.com.audiobook.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import axiom.com.audiobook.R;

public abstract class MyBottomNavigationActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.navigation_search:
                if (!((Activity) this instanceof SearchActivity)) {
                    startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                }
                break;
            case R.id.navigation_more_categories:
                if (!((Activity) this instanceof CategoryListActivity)) {
                    startActivity(new Intent(getApplicationContext(), CategoryListActivity.class));
                    break;
                }
            case R.id.navigation_downloads:

                startActivity(new Intent(getApplicationContext(), DownloadsActivity.class));
                break;

            case R.id.navigation_favourites:
                startActivity(new Intent(getApplicationContext(), DownloadsActivity.class)
                        .putExtra(getString(R.string.activity_name_extra_text), getString(R.string.favourites_text)));
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!((Activity) this instanceof MainActivity)) {
            startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        return;
    }

}
