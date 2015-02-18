package com.example.jake.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.jake.basesyncadapter.provider.FileContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jvanburen on 2/13/2015.
 */
public class FileDatabase {
    private final String TAG = "FileDatabase";

    private Context _context;
    private FilesSqliteOpenHelper _dbHelper;
    private SQLiteDatabase _database;

    public FileDatabase(Context context) {
        _context = context;
        _dbHelper = new FilesSqliteOpenHelper(context);
    }

    public void open() {
        try {
            if (_database == null || !_database.isOpen()) {
                _database = _dbHelper.getWritableDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            _dbHelper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Entry> getRootEntries() {
        String where = FilesSqliteOpenHelper.PARENT_ID_COLUMN + " is null";
        return getEntries(where);
    }

    public Entry getEntry(String entryId) {
        Entry result = null;
        String where = FilesSqliteOpenHelper.FOLDER_ID_COLUMN + " ='" + entryId + "'";
        open();
        Cursor cursor = _database.query(FilesSqliteOpenHelper.FOLDERS_TABLE, null, where, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result = getFolderEntry(cursor);
            break;
        }
        cursor.close();
        return result;
    }

    public List<Entry> getSubEntries(String parentId) {
        String where = FilesSqliteOpenHelper.PARENT_ID_COLUMN + " ='" + parentId + "'";
        return getEntries(where);
    }

    private List<Entry> getEntries(String whereClause) {
        List<Entry> messages = new ArrayList<>();

        open();
        Cursor cursor = _database.query(FilesSqliteOpenHelper.FOLDERS_TABLE, null, whereClause, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            messages.add(getFolderEntry(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        cursor = _database.query(FilesSqliteOpenHelper.FILES_TABLE, null, whereClause, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            messages.add(getFileEntry(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return messages;
    }

    private Entry getFolderEntry(Cursor cursor) {
        Entry entry = new Entry();
        entry.Id = cursor.getInt(cursor.getColumnIndex(FilesSqliteOpenHelper.ID_COLUMN));
        entry.EntryId = cursor.getString(cursor.getColumnIndex(FilesSqliteOpenHelper.FOLDER_ID_COLUMN));
        entry.EntryName = cursor.getString(cursor.getColumnIndex(FilesSqliteOpenHelper.FOLDER_NAME_COLUMN));
        entry.ParentId = cursor.getString(cursor.getColumnIndex(FilesSqliteOpenHelper.PARENT_ID_COLUMN));
        entry.FileType = FileContract.FileType.Directory;
        return entry;
    }

    private Entry getFileEntry(Cursor cursor) {
        Entry entry = new Entry();
        entry.Id = cursor.getInt(cursor.getColumnIndex(FilesSqliteOpenHelper.ID_COLUMN));
        entry.EntryId = cursor.getString(cursor.getColumnIndex(FilesSqliteOpenHelper.FILE_ID_COLUMN));
        entry.EntryName = cursor.getString(cursor.getColumnIndex(FilesSqliteOpenHelper.FILE_NAME_COLUMN));
        entry.ParentId = cursor.getString(cursor.getColumnIndex(FilesSqliteOpenHelper.PARENT_ID_COLUMN));
        entry.FileType = FileContract.FileType.File;
        return entry;
    }

    public void addFile(String fileId, String parentId, String fileName) {
        try {
            if (exists(FilesSqliteOpenHelper.FILES_TABLE, FilesSqliteOpenHelper.FILE_ID_COLUMN, fileId)) {
                return;
            }
            open();
            ContentValues contentValues = FilesSqliteOpenHelper.getFileInsertContentValues(fileId, parentId, fileName);
            long success = _database.insert(FilesSqliteOpenHelper.FILES_TABLE, null, contentValues);
            Log.d(TAG, "Add file, Success=" + success);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFolder(String folderId, String parentId, String fileName) {
        try {
            if (exists(FilesSqliteOpenHelper.FOLDERS_TABLE, FilesSqliteOpenHelper.FOLDER_ID_COLUMN, folderId)) {
                return;
            }
            open();
            ContentValues contentValues = FilesSqliteOpenHelper.getFolderInsertContentValues(folderId, parentId, fileName);
            long success = _database.insert(FilesSqliteOpenHelper.FOLDERS_TABLE, null, contentValues);
            Log.d(TAG, "Add folder, Success=" + success);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean exists(String tableName, String idColumnName, String itemId) {
        open();
        int count = 0;
        try {
            String countQuery = "SELECT  * FROM " + tableName + " WHERE " + idColumnName + "='" + itemId + "'";
            Cursor cursor = _database.rawQuery(countQuery, null);
            count = cursor.getCount();
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count > 0;
    }
}
