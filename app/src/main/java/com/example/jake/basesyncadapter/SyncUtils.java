package com.example.jake.basesyncadapter;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.example.jake.basesyncadapter.provider.CloudServicesContract;
import com.example.jake.common.accounts.CloudServiceAccountUtils;


/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {
    private static final long SYNC_FREQUENCY = 60 * 60;  // 1 hour (in seconds)
    //private static final String CONTENT_AUTHORITY = FeedContract.CONTENT_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    // Value below must match the account type specified in res/xml/syncadapter.xml
    public static final String ACCOUNT_TYPE = "com.example.android.basicsyncadapter.account";

    /**
     * Helper method to trigger an immediate sync ("refresh").
     * <p/>
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     * <p/>
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void TriggerRefresh(Context context) {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        for (Account account : CloudServiceAccountUtils.getAndroidAccounts(context)) {
            ContentResolver.requestSync(
                    account, // Sync account
                    CloudServicesContract.AUTHORITY,                 // Content authority
                    b);                                             // Extras
        }
    }

    public static void addPeriodicRefresh(Context context) {
        for (Account account : CloudServiceAccountUtils.getAndroidAccounts(context)) {
            ContentResolver.addPeriodicSync(
                    account, // Sync account
                    CloudServicesContract.AUTHORITY,
                    Bundle.EMPTY, 30000);
        }
    }
}
