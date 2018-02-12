package axiom.com.audiobook.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import axiom.com.audiobook.R;
import axiom.com.audiobook.data.NetworkUtils;

public class NetworkConnectionActivity extends AppCompatActivity {

    private NetworkInfoReceiver mNetworkInfoReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_layout);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        mNetworkInfoReceiver = new NetworkInfoReceiver();
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkInfoReceiver, networkFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkInfoReceiver);
    }

    private class NetworkInfoReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (NetworkUtils.isNetworkConnected(context)) {
                    finish();
                }
            }
        }
    }
}
