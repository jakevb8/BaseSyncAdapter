package com.example.jake.basesyncadapter.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jvanburen on 2/16/2015.
 */
public class FileContract {
    public class FileType {
        public static final int Unknown = 0;
        public static final int File = 1;
        public static final int Directory = 2;
        public static final int Account = 3;
    }

    public static final String EXTRA_IS_ROOT = "is_root";
    public static final String EXTRA_PARENT_ID = "parent_id";
    public static final String EXTRA_PARENT_NAME = "parent_name";

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.example.jake.basesyncadapter";

    /**
     * Base URI. (content://com.example.android.basicsyncadapter)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "entry"-type resources..
     */
    private static final String PATH_ENTRIES = "files";

    /**
     * Columns supported by "entries" records.
     */
    public static class Entry implements BaseColumns {
        /**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.basesyncadapter.entries";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.basesyncadapter.entry";

        /**
         * Fully qualified URI for "entry" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRIES).build();

        /**
         * Table name where records are stored for "entry" resources.
         */
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_ENTRY_ID = "entry_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LINK = "link";
        public static final String COLUMN_PARENT_ID = "parent_id";
        public static final String COLUMN_FILE_TYPE = "file_type";

        public static final String[] COLUMNS = new String[]{_ID,
                COLUMN_NAME_ENTRY_ID, COLUMN_NAME_TITLE, COLUMN_PARENT_ID, COLUMN_FILE_TYPE};

        public static Object[] getValues(int id, String entryId, String title, String parendId, int fileType) {
            Object[] values = new Object[COLUMNS.length];
            values[0] = id;
            values[1] = entryId;
            values[2] = title;
            values[3] = parendId;
            values[4] = fileType;
            return values;
        }
    }
}
