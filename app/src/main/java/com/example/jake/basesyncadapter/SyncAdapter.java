package com.example.jake.basesyncadapter;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.example.jake.basesyncadapter.provider.CloudServicesContract;
import com.example.jake.common.accounts.AccountInfo;
import com.example.jake.common.accounts.CloudServiceAccountUtils;
import com.example.jake.common.db.FileDatabase;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    private static final String FEED_URL = "http://android-developers.blogspot.com/atom.xml";

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Project used when querying content provider. Returns all known fields.
     */
//    private static final String[] PROJECTION = new String[] {
//            FeedContract.Entry._ID,
//            FeedContract.Entry.COLUMN_NAME_ENTRY_ID,
//            FeedContract.Entry.COLUMN_NAME_TITLE,
//            FeedContract.Entry.COLUMN_NAME_LINK,
//            FeedContract.Entry.COLUMN_NAME_PUBLISHED};

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;
    public static final int COLUMN_TITLE = 2;
    public static final int COLUMN_LINK = 3;
    public static final int COLUMN_PUBLISHED = 4;

    private Context _context;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        _context = context;
        mContentResolver = context.getContentResolver();
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        _context = context;
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        try {
            final URL location = new URL(FEED_URL);
            InputStream stream = null;

            try {
                FileDatabase fileDatabase = new FileDatabase(getContext());
                addFiles(provider, fileDatabase, account, null);
                fileDatabase.close();

                //Log.i(TAG, "Streaming data from network: " + location);
                //stream = downloadUrl(location);
                //updateLocalFeedData(stream, syncResult);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Feed URL is malformed", e);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (IOException e) {
            Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } //catch (XmlPullParserException e) {
//            Log.e(TAG, "Error parsing feed: " + e.toString());
//            syncResult.stats.numParseExceptions++;
//            return;
//        } catch (ParseException e) {
//            Log.e(TAG, "Error parsing feed: " + e.toString());
//            syncResult.stats.numParseExceptions++;
//            return;
//        } catch (RemoteException e) {
//            Log.e(TAG, "Error updating database: " + e.toString());
//            syncResult.databaseError = true;
//            return;
//        } catch (OperationApplicationException e) {
//            Log.e(TAG, "Error updating database: " + e.toString());
//            syncResult.databaseError = true;
//            return;
//        }
        Log.i(TAG, "Network synchronization complete");
    }

    private void addFiles(ContentProviderClient provider, FileDatabase fileDatabase, Account account, String parentId) {
        Cursor cursor = null;
        try {
            if (parentId == null) {
                cursor = provider.query(Uri.parse(CloudServicesContract.Browse.CONTENT_URI + "/" + account.name), null, null, null, null);
            } else {
                cursor = provider.query(Uri.parse(CloudServicesContract.Browse.CONTENT_URI + "/" + account.name + "/" + parentId), null, null, null, null);
            }
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String itemId = cursor.getString(cursor.getColumnIndex(CloudServicesContract.Browse.ITEM_ID));
                    String title = cursor.getString(cursor.getColumnIndex(CloudServicesContract.Browse.TITLE));
                    int fileType = cursor.getInt(cursor.getColumnIndex(CloudServicesContract.Browse.TYPE));
                    switch (fileType) {
                        case 0:
                            break;
                        case 1:
                            fileDatabase.addFile(account.name, itemId, parentId, title);
                            break;
                        case 2:
                            fileDatabase.addFolder(account.name, itemId, parentId, title);
                            addFiles(provider, fileDatabase, account, itemId);
                            break;
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void updateLocalFeedData(final InputStream stream, final SyncResult syncResult)
            throws IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException {
//        final FeedParser feedParser = new FeedParser();
//        final ContentResolver contentResolver = getContext().getContentResolver();
//
//        Log.i(TAG, "Parsing stream as Atom feed");
//        final List<FeedParser.Entry> entries = feedParser.parse(stream);
//        Log.i(TAG, "Parsing complete. Found " + entries.size() + " entries");
//
//
//        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
//
//        // Build hash table of incoming entries
//        HashMap<String, FeedParser.Entry> entryMap = new HashMap<String, FeedParser.Entry>();
//        for (FeedParser.Entry e : entries) {
//            entryMap.put(e.id, e);
//        }
//
//        // Get list of all items
//        Log.i(TAG, "Fetching local entries for merge");
//        Uri uri = FeedContract.Entry.CONTENT_URI; // Get all entries
//        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
//        assert c != null;
//        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");
//
//        // Find stale data
//        int id;
//        String entryId;
//        String title;
//        String link;
//        long published;
//        while (c.moveToNext()) {
//            syncResult.stats.numEntries++;
//            id = c.getInt(COLUMN_ID);
//            entryId = c.getString(COLUMN_ENTRY_ID);
//            title = c.getString(COLUMN_TITLE);
//            link = c.getString(COLUMN_LINK);
//            published = c.getLong(COLUMN_PUBLISHED);
//            FeedParser.Entry match = entryMap.get(entryId);
//            if (match != null) {
//                // Entry exists. Remove from entry map to prevent insert later.
//                entryMap.remove(entryId);
//                // Check to see if the entry needs to be updated
//                Uri existingUri = FeedContract.Entry.CONTENT_URI.buildUpon()
//                        .appendPath(Integer.toString(id)).build();
//                if ((match.title != null && !match.title.equals(title)) ||
//                        (match.link != null && !match.link.equals(link)) ||
//                        (match.published != published)) {
//                    // Update existing record
//                    Log.i(TAG, "Scheduling update: " + existingUri);
//                    batch.add(ContentProviderOperation.newUpdate(existingUri)
//                            .withValue(FeedContract.Entry.COLUMN_NAME_TITLE, title)
//                            .withValue(FeedContract.Entry.COLUMN_NAME_LINK, link)
//                            .withValue(FeedContract.Entry.COLUMN_NAME_PUBLISHED, published)
//                            .build());
//                    syncResult.stats.numUpdates++;
//                } else {
//                    Log.i(TAG, "No action: " + existingUri);
//                }
//            } else {
//                // Entry doesn't exist. Remove it from the database.
//                Uri deleteUri = FeedContract.Entry.CONTENT_URI.buildUpon()
//                        .appendPath(Integer.toString(id)).build();
//                Log.i(TAG, "Scheduling delete: " + deleteUri);
//                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
//                syncResult.stats.numDeletes++;
//            }
//        }
//        c.close();
//
//        // Add new items
//        for (FeedParser.Entry e : entryMap.values()) {
//            Log.i(TAG, "Scheduling insert: entry_id=" + e.id);
//            batch.add(ContentProviderOperation.newInsert(FeedContract.Entry.CONTENT_URI)
//                    .withValue(FeedContract.Entry.COLUMN_NAME_ENTRY_ID, e.id)
//                    .withValue(FeedContract.Entry.COLUMN_NAME_TITLE, e.title)
//                    .withValue(FeedContract.Entry.COLUMN_NAME_LINK, e.link)
//                    .withValue(FeedContract.Entry.COLUMN_NAME_PUBLISHED, e.published)
//                    .build());
//            syncResult.stats.numInserts++;
//        }
//        Log.i(TAG, "Merge solution ready. Applying batch update");
//        mContentResolver.applyBatch(FeedContract.CONTENT_AUTHORITY, batch);
//        mContentResolver.notifyChange(
//                FeedContract.Entry.CONTENT_URI, // URI where data was modified
//                null,                           // No local observer
//                false);                         // IMPORTANT: Do not sync to network
//        // This sample doesn't support uploads, but if *your* code does, make sure you set
//        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }

//    private InputStream downloadUrl(final URL url) throws IOException {
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
//        conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
//        conn.setRequestMethod("GET");
//        conn.setDoInput(true);
//        // Starts the query
//        conn.connect();
//        return conn.getInputStream();
//    }
}
