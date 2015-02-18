package com.example.jake.basesyncadapter.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Base64;

import com.example.jake.common.CloudFileInfo;
import com.example.jake.common.CloudFileUrlInfo;
import com.example.jake.common.CloudUtils;
import com.example.jake.common.IReadableCloudStorage;
import com.example.jake.common.StorageQuotaInfo;
import com.example.jake.common.accounts.AccountInfo;
import com.example.jake.common.accounts.CloudServiceAccountUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CloudServicesContentProvider extends ContentProvider {
    private static final UriMatcher _uriMatcher;
    private static final int ACCOUNTS = 1;
    private static final int BROWSE_ROOT = 2;
    private static final int BROWSE_FOLDER = 3;
    private static final int URL = 4;

    static {
        _uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        _uriMatcher.addURI(CloudServicesContract.AUTHORITY,
                CloudServicesContract.Accounts.CONTENT_DIRECTORY, ACCOUNTS);
        _uriMatcher.addURI(CloudServicesContract.AUTHORITY,
                CloudServicesContract.Browse.CONTENT_DIRECTORY + "/*", BROWSE_ROOT);
        _uriMatcher.addURI(CloudServicesContract.AUTHORITY,
                CloudServicesContract.Browse.CONTENT_DIRECTORY + "/*/*", BROWSE_FOLDER);
        _uriMatcher.addURI(CloudServicesContract.AUTHORITY,
                CloudServicesContract.Url.CONTENT_DIRECTORY + "/*/*", URL);
    }

    private IReadableCloudStorage _cloudStorage;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        return 0;
    }

    @Override
    public String getType(Uri uri) {
        switch (_uriMatcher.match(uri)) {
            case ACCOUNTS:
                return CloudServicesContract.Accounts.CONTENT_TYPE;
            case BROWSE_ROOT:
            case BROWSE_FOLDER:
                return CloudServicesContract.Browse.CONTENT_TYPE;
            case URL:
                return CloudServicesContract.Url.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        List<String> pathSegments = uri.getPathSegments();
        try {
            switch (_uriMatcher.match(uri)) {
                case ACCOUNTS:
                    return getAccountsCursor();
                case BROWSE_ROOT:
                case BROWSE_FOLDER:
                    return getBrowseCursor(pathSegments);
                case URL:
                    return getUrlCursor(pathSegments);
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Cursor getAccountsCursor() {
        MatrixCursor result = new MatrixCursor(CloudServicesContract.Accounts.COLUMNS);


        ArrayList<AccountInfo> accountInfos = CloudServiceAccountUtils.getAuthenticatedAccounts(getContext());
        for (AccountInfo accountInfo : accountInfos) {
            Object[] values = new Object[CloudServicesContract.Accounts.COLUMNS.length];
            values[0] = Base64.encodeToString(accountInfo.AccountId.getBytes(), Base64.NO_WRAP);
            values[1] = accountInfo.Provider.ordinal();
            values[2] = accountInfo.AccountName;

            _cloudStorage = CloudUtils.getCloudStorage(getContext(), accountInfo.Provider);
            values[3] = _cloudStorage.getDisplayName() + ": " + accountInfo.AccountName;

            try {
                StorageQuotaInfo storageQuotaInfo = _cloudStorage.getStorageQuotaInfo(accountInfo);

                values[4] = (storageQuotaInfo.totalQuota != 0) ? 1 : 0;
                values[5] = storageQuotaInfo.usedQuota;
                values[6] = storageQuotaInfo.totalQuota;
            } catch (Exception e) {
                values[4] = 0;
                values[5] = 0;
                values[6] = 0;
                e.printStackTrace();
            }

            result.addRow(values);
        }

        return result;
    }

    private Cursor getBrowseCursor(List<String> pathSegments) {
        MatrixCursor result = new MatrixCursor(CloudServicesContract.Browse.COLUMNS);

        String accountId = new String(Base64.decode(pathSegments.get(1), Base64.NO_WRAP));
        String folderId = "";
        if (pathSegments.size() > 2) {
            folderId = new String(Base64.decode(pathSegments.get(2), Base64.NO_WRAP));
        }
        AccountInfo accountInfo = CloudServiceAccountUtils.getAccount(getContext(), accountId);
        _cloudStorage = CloudUtils.getCloudStorage(getContext(), accountInfo.Provider);
        ArrayList<CloudFileInfo> cloudFileInfos = _cloudStorage.getFiles(accountInfo, folderId);

        for (CloudFileInfo cloudFileInfo : cloudFileInfos) {
            Object[] values = new Object[CloudServicesContract.Browse.COLUMNS.length];
            values[0] = Base64.encodeToString(cloudFileInfo.ItemId.getBytes(), Base64.NO_WRAP);
            values[1] = cloudFileInfo.Title;
            switch (cloudFileInfo.Type) {
                case 0:
                    values[2] = FileContract.FileType.Unknown;
                    break;
                case 1:
                    values[2] = FileContract.FileType.File;
                    break;
                case 2:
                    values[2] = FileContract.FileType.Directory;
                    break;
            }
            values[3] = cloudFileInfo.Size;
            values[4] = cloudFileInfo.DateLastModified;
            result.addRow(values);
        }

        return result;
    }

    private Cursor getUrlCursor(List<String> pathSegments) {
        String[] columnNames = new String[7];
        columnNames[0] = CloudServicesContract.Url.ITEM_ID;
        columnNames[1] = CloudServicesContract.Url.URL;
        columnNames[2] = CloudServicesContract.Url.HEADERS;
        columnNames[3] = CloudServicesContract.Url.TITLE;
        columnNames[4] = CloudServicesContract.Url.SIZE;
        columnNames[5] = CloudServicesContract.Url.DATE_LAST_MODIFIED;
        columnNames[6] = CloudServicesContract.Url.TYPE;

        MatrixCursor result = new MatrixCursor(columnNames);

        String accountId = new String(Base64.decode(pathSegments.get(1), Base64.NO_WRAP));
        String itemId = new String(Base64.decode(pathSegments.get(2), Base64.NO_WRAP));
        AccountInfo accountInfo = CloudServiceAccountUtils.getAccount(getContext(), accountId);
        _cloudStorage = CloudUtils.getCloudStorage(getContext(), accountInfo.Provider);
        CloudFileUrlInfo urlInfo = _cloudStorage.getUrlInfo(accountInfo, itemId);
        Object[] values = new Object[columnNames.length];
        values[0] = urlInfo.ItemId;
        values[1] = urlInfo.Url;

        try {
            values[2] = new Gson().toJson(urlInfo.Headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        values[3] = urlInfo.Title;
        values[4] = urlInfo.Size;
        values[5] = urlInfo.DateLastModified;
        values[6] = urlInfo.Type;

        result.addRow(values);

        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
