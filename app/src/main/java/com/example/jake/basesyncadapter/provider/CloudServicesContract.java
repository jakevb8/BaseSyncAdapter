package com.example.jake.basesyncadapter.provider;

import android.content.ContentResolver;
import android.net.Uri;

public final class CloudServicesContract {
	public static final String AUTHORITY = "com.example.jake.provider";

	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	public static class Accounts {
		public static final String ACCOUNT_ID = "account_id";

		public static final String PROVIDER = "provider";

		public static final String TITLE = "title";

		public static final String DESCRIPTION = "description";

		public static final String CONNECTED = "connected";

		public static final String SPACE_USED = "space_used";

		public static final String TOTAL_SPACE = "total_space";

		public static final String CONTENT_DIRECTORY = "accounts";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY);

		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY
				+ "." + CONTENT_DIRECTORY;

		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd."
				+ AUTHORITY + "." + CONTENT_DIRECTORY;

		public static final String[] COLUMNS = { ACCOUNT_ID, PROVIDER, TITLE, DESCRIPTION, CONNECTED,
				SPACE_USED, TOTAL_SPACE };
	}

	public static class Browse {
		public static final String ITEM_ID = "item_id";

		public static final String TITLE = "title";

		public static final String TYPE = "type";

		public static final String SIZE = "size";

		public static final String DATE_LAST_MODIFIED = "date_lastmod";

		public static final String CONTENT_DIRECTORY = "browse";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY);

		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY
				+ "." + CONTENT_DIRECTORY;

		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd."
				+ AUTHORITY + "." + CONTENT_DIRECTORY;

		public static final String[] COLUMNS = { ITEM_ID, TITLE, TYPE, SIZE, DATE_LAST_MODIFIED };
	}

	public static class Url {
		public static final String ITEM_ID = "item_id";

		public static final String TITLE = "title";

		public static final String TYPE = "type";

		public static final String SIZE = "size";

		public static final String DATE_LAST_MODIFIED = "date_lastmod";;

		public static final String URL = "url";

		public static final String HEADERS = "headers";

		public static final String CONTENT_DIRECTORY = "url";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_DIRECTORY);

		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + AUTHORITY
				+ "." + CONTENT_DIRECTORY;

		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd."
				+ AUTHORITY + "." + CONTENT_DIRECTORY;

		public static final String[] COLUMNS = { ITEM_ID, TITLE, TYPE, SIZE, DATE_LAST_MODIFIED, URL, HEADERS };
	}
}
