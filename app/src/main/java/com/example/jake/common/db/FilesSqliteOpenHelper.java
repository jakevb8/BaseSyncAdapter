package com.example.jake.common.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jvanburen on 2/13/2015.
 */
public class FilesSqliteOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "files.db";
    private static final int DATABASE_VERSION = 1;
    public static final String ID_COLUMN = "id";
    public static final String PARENT_ID_COLUMN = "parent_id";

    public static final String FOLDERS_TABLE = "folders";
    public static final String FOLDER_ID_COLUMN = "folder_id";
    public static final String FOLDER_NAME_COLUMN = "folder_name";
    private static final String FOLDERS_TABLE_CREATE = "create table "
            + FOLDERS_TABLE + "(" + ID_COLUMN + " integer primary key autoincrement, " + FOLDER_ID_COLUMN + " text, "
            + PARENT_ID_COLUMN + " text, " +FOLDER_NAME_COLUMN + " text);";

    public static final String FILES_TABLE = "files";
    public static final String FILE_ID_COLUMN = "file_id";
    public static final String FILE_NAME_COLUMN = "file_name";
    private static final String FILES_TABLE_CREATE = "create table "
            + FILES_TABLE + "(" + ID_COLUMN + " integer primary key autoincrement, " + FILE_ID_COLUMN + " text, "
            + PARENT_ID_COLUMN + " text, " + FILE_NAME_COLUMN + " text);";

    public FilesSqliteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FOLDERS_TABLE_CREATE);
        db.execSQL(FILES_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + FILES_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + FOLDERS_TABLE);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ContentValues getFolderInsertContentValues(String folderId, String parentId, String folderName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FOLDER_ID_COLUMN, folderId);
        if(parentId != null) {
            contentValues.put(PARENT_ID_COLUMN, parentId);
        }
        contentValues.put(FOLDER_NAME_COLUMN, folderName);
        return  contentValues;
    }

    public static ContentValues getFileInsertContentValues(String fileId, String parentId, String fileName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FILE_ID_COLUMN, fileId);
        if(parentId != null) {
            contentValues.put(PARENT_ID_COLUMN, parentId);
        }
        contentValues.put(FILE_NAME_COLUMN, fileName);
        return  contentValues;
    }
}
