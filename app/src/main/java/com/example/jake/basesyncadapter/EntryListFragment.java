package com.example.jake.basesyncadapter;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.jake.basesyncadapter.provider.FileContract;
import com.example.jake.common.accounts.CloudServiceAccountUtils;

public class EntryListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "EntryListFragment";
    private static final int ROOT_DIRECTORY = 0;
    private static final int SUB_DIRECTORY = 1;
    private static final String EXTRA_PARENT_ID = "parent_id";

    private BroadcastReceiver _backPressedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(_parentId != null) {
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_PARENT_ID, _parentId);
                getLoaderManager().restartLoader(SUB_DIRECTORY, bundle, EntryListFragment.this);
            }
            else {
                getLoaderManager().restartLoader(ROOT_DIRECTORY, null, EntryListFragment.this);
            }
        }
    };
    private SimpleCursorAdapter _adapter;
    private Object _syncObserverHandle;
    private Menu _optionsMenu;
    private String _parentId;

    /**
     * List of Cursor columns to read from when preparing an adapter to populate the ListView.
     */
    private static final String[] FROM_COLUMNS = new String[]{
            FileContract.Entry.COLUMN_NAME_TITLE,
            FileContract.Entry.COLUMN_NAME_ENTRY_ID
    };

    /**
     * List of Views which will be populated by Cursor data.
     */
    private static final int[] TO_FIELDS = new int[]{
            android.R.id.text1,
            android.R.id.text2};

    public EntryListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SyncUtils.addPeriodicRefresh(getActivity());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        SyncUtils.TriggerRefresh(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _adapter = new SimpleCursorAdapter(
                getActivity(),       // Current context
                android.R.layout.simple_list_item_activated_2,  // Layout for individual rows
                null,                // Cursor
                FROM_COLUMNS,        // Cursor columns to use
                TO_FIELDS,           // Layout fields to use
                0                    // No flags
        );
        _adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                Log.d(TAG, "Column: " + i + " type: " + cursor.getType(i));
//                if (i == COLUMN_PUBLISHED) {
//                    // Convert timestamp to human-readable date
//                    Time t = new Time();
//                    t.set(cursor.getLong(i));
//                    ((TextView) view).setText(t.format("%Y-%m-%d %H:%M"));
//                    return true;
//                } else {
//                    // Let SimpleCursorAdapter handle other fields automatically
//                    return false;
//                }
                return false;
            }
        });
        setListAdapter(_adapter);
        setEmptyText(getText(R.string.loading));
        getLoaderManager().initLoader(ROOT_DIRECTORY, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        _syncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(_backPressedReceiver, new IntentFilter(EntryListActivity.ACTION_BACK_PRESSED));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (_syncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(_syncObserverHandle);
            _syncObserverHandle = null;
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_backPressedReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int which, Bundle bundle) {
        // We only have one loader, so we can ignore the value of i.
        // (It'll be '0', as set in onCreate().)
        switch (which) {
            case ROOT_DIRECTORY:
                return new CursorLoader(getActivity(), FileContract.Entry.CONTENT_URI, null, null, null, null);
            case SUB_DIRECTORY:
                if (bundle.containsKey(EXTRA_PARENT_ID)) {
                    return new CursorLoader(getActivity(), Uri.parse(FileContract.Entry.CONTENT_URI + "/" +
                            bundle.getString(EXTRA_PARENT_ID)), null, null, null, null);
                }
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(cursor.getExtras() != null && cursor.getExtras().containsKey(FileContract.Entry.COLUMN_PARENT_ID)) {
            _parentId = cursor.getExtras().getString(FileContract.Entry.COLUMN_PARENT_ID);
        }
        _adapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        _adapter.changeCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        _optionsMenu = menu;
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                SyncUtils.TriggerRefresh(getActivity().getApplicationContext());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Cursor c = (Cursor) _adapter.getItem(position);
        int fileType = c.getInt(c.getColumnIndex(FileContract.Entry.COLUMN_FILE_TYPE));
        if(fileType == FileContract.FileType.Directory) {
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_PARENT_ID, c.getString(c.getColumnIndex(FileContract.Entry.COLUMN_NAME_ENTRY_ID)));
            getLoaderManager().restartLoader(SUB_DIRECTORY, bundle, this);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setRefreshActionButtonState(boolean refreshing) {
        if (_optionsMenu == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }

        final MenuItem refreshItem = _optionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account[] accounts = CloudServiceAccountUtils.getAndroidAccounts(getActivity().getApplicationContext());
                    if (accounts == null || accounts.length == 0) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setRefreshActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            accounts[0], FileContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            accounts[0], FileContract.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

}