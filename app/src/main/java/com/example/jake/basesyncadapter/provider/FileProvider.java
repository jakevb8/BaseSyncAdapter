package com.example.jake.basesyncadapter.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.example.jake.common.accounts.AccountInfo;
import com.example.jake.common.accounts.CloudServiceAccountUtils;
import com.example.jake.common.db.Entry;
import com.example.jake.common.db.FileDatabase;
import com.example.jake.common.db.SelectionBuilder;

import java.util.List;

/**
 * Created by jvanburen on 2/16/2015.
 */
public class FileProvider extends ContentProvider {

    private static final String AUTHORITY = FileContract.CONTENT_AUTHORITY;

    public static final int ROOT_ENTRIES = 1;

    public static final int ENTRIES_ID = 2;

    private static final UriMatcher _uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        _uriMatcher.addURI(AUTHORITY, "files/*", ROOT_ENTRIES);
        _uriMatcher.addURI(AUTHORITY, "files/*/*", ENTRIES_ID);
    }

    private FileDatabase _fileDatabase;

    @Override
    public boolean onCreate() {
        _fileDatabase = new FileDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int uriMatch = _uriMatcher.match(uri);
        if(uri.getPathSegments().size() == 0) {
            return null;
        }
        String accountId = uri.getPathSegments().get(1);
        switch (uriMatch) {
            case ENTRIES_ID:
                String entryId = uri.getLastPathSegment();
                Cursor entriesCursor =  getEntryCursor(uri, _fileDatabase.getSubEntries(accountId, entryId));
                Entry entry = _fileDatabase.getEntry(accountId, entryId);
                entriesCursor.getExtras().putBoolean(FileContract.EXTRA_IS_ROOT, false);
                entriesCursor.getExtras().putString(FileContract.EXTRA_PARENT_ID, entry.ParentId);
                if(entry.ParentId == null) {
                    entriesCursor.getExtras().putBoolean(FileContract.EXTRA_IS_ROOT, true);
                }
                entriesCursor.getExtras().putString(FileContract.EXTRA_PARENT_NAME, entry.EntryName);
                return entriesCursor;
            case ROOT_ENTRIES:
                Cursor rootCursor = getEntryCursor(uri, _fileDatabase.getRootEntries(accountId));
                rootCursor.getExtras().putBoolean(FileContract.EXTRA_IS_ROOT, true);
                AccountInfo accountInfo = CloudServiceAccountUtils.getAccount(getContext(), accountId);
                rootCursor.getExtras().putString(FileContract.EXTRA_PARENT_NAME, accountInfo.AccountName);
                return  rootCursor;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private Cursor getEntryCursor(Uri uri, List<Entry> entries) {
        MatrixCursor result = new MatrixCursor(FileContract.Entry.COLUMNS);
        for (Entry entry : entries) {
            result.addRow(FileContract.Entry.getValues(entry.Id, entry.EntryId, entry.EntryName, entry.ParentId, entry.FileType));
        }
        Context context = getContext();
        assert context != null;
        result.setNotificationUri(context.getContentResolver(), uri);
        return result;
    }

    @Override
    public String getType(Uri uri) {
        final int match = _uriMatcher.match(uri);
        switch (match) {
            case ROOT_ENTRIES:
            case ENTRIES_ID:
                return FileContract.Entry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = _uriMatcher.match(uri);
        Uri result = null;
        switch (match) {
            case ROOT_ENTRIES:
                //long id = db.insertOrThrow(FeedContract.Entry.TABLE_NAME, null, values);
                //result = Uri.parse(FeedContract.Entry.CONTENT_URI + "/" + id);
                break;
            case ENTRIES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
//        SelectionBuilder builder = new SelectionBuilder();
//        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
//        final int match = sUriMatcher.match(uri);
//        int count;
//        switch (match) {
//            case ROUTE_ENTRIES:
//                count = builder.table(FeedContract.Entry.TABLE_NAME)
//                        .where(selection, selectionArgs)
//                        .delete(db);
//                break;
//            case ROUTE_ENTRIES_ID:
//                String id = uri.getLastPathSegment();
//                count = builder.table(FeedContract.Entry.TABLE_NAME)
//                        .where(FeedContract.Entry._ID + "=?", id)
//                        .where(selection, selectionArgs)
//                        .delete(db);
//                break;
//            default:
//                throw new UnsupportedOperationException("Unknown uri: " + uri);
//        }
//        // Send broadcast to registered ContentObservers, to refresh UI.
//        Context ctx = getContext();
//        assert ctx != null;
//        ctx.getContentResolver().notifyChange(uri, null, false);
//        return count;
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        SelectionBuilder builder = new SelectionBuilder();
//        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
//        final int match = sUriMatcher.match(uri);
//        int count;
//        switch (match) {
//            case ROUTE_ENTRIES:
//                count = builder.table(FeedContract.Entry.TABLE_NAME)
//                        .where(selection, selectionArgs)
//                        .update(db, values);
//                break;
//            case ROUTE_ENTRIES_ID:
//                String id = uri.getLastPathSegment();
//                count = builder.table(FeedContract.Entry.TABLE_NAME)
//                        .where(FeedContract.Entry._ID + "=?", id)
//                        .where(selection, selectionArgs)
//                        .update(db, values);
//                break;
//            default:
//                throw new UnsupportedOperationException("Unknown uri: " + uri);
//        }
//        Context ctx = getContext();
//        assert ctx != null;
//        ctx.getContentResolver().notifyChange(uri, null, false);
        return 0;//count;
    }
}
