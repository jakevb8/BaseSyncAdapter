package com.example.jake.basesyncadapter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Activity for holding EntryListFragment.
 */
public class EntryListActivity extends FragmentActivity {
    public static final String ACTION_BACK_PRESSED = "com.example.jake.basesyncadapter.EntryListActivity:ACTION_BACK_PRESSED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);
    }

    @Override
    public void onBackPressed() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_BACK_PRESSED));
    }
}
