package com.example.jake.common.accounts;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.jake.basesyncadapter.CloudProviders;


public class CloudServiceAccountUtils {
    public static final String ACCOUNT_TYPE = "com.example.jake";
    public static final String KEY_ACCOUNT_NAME = "account_name";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_PROVIDER = "provider";

    public static boolean addAccount(Context context, String accountId, String accountName, String accessToken,
                                  String refreshToken, CloudProviders cloudProvider) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = new Account(accountId, ACCOUNT_TYPE);
        Bundle extras = new Bundle();
        extras.putString(KEY_ACCOUNT_NAME, accountName);
        extras.putString(KEY_ACCESS_TOKEN, accessToken);
        extras.putString(KEY_REFRESH_TOKEN, refreshToken);
        extras.putInt(KEY_PROVIDER, cloudProvider.ordinal());

        return accountManager.addAccountExplicitly(account, null, extras);
    }

    public static void updateTokens(Context context, AccountInfo accountInfo, String accessToken, String refreshToken) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = new Account(accountInfo.AccountId, ACCOUNT_TYPE);
        accountManager.setUserData(account, KEY_ACCESS_TOKEN, accessToken);
        accountManager.setUserData(account, KEY_REFRESH_TOKEN, refreshToken);
    }

	public static void removeAccount(Context applicationContext, AccountInfo accountInfo) {
        AccountManager accountManager = AccountManager.get(applicationContext);
        Account account = new Account(accountInfo.AccountId, ACCOUNT_TYPE);
        accountManager.removeAccount(account, null, null);
	}

	public static AccountInfo getAccount(Context applicationContext, String accountId) {
		AccountInfo result = new AccountInfo();
        AccountManager accountManager = AccountManager.get(applicationContext);
        Account account = new Account(accountId, ACCOUNT_TYPE);
        return getAccount(applicationContext, account);
	}

    public static AccountInfo getAccount(Context applicationContext, Account account) {
        AccountInfo result = new AccountInfo();
        AccountManager accountManager = AccountManager.get(applicationContext);
        result.AccountId = account.name;
        result.AccessToken = accountManager.getUserData(account, KEY_ACCESS_TOKEN);
        result.RefreshToken = accountManager.getUserData(account, KEY_REFRESH_TOKEN);
        result.AccountName = accountManager.getUserData(account, KEY_ACCOUNT_NAME);
        result.Provider = CloudProviders.GoogleDrive;
        return result;
    }


    public static ArrayList<AccountInfo> getAuthenticatedAccounts(Context applicationContext) {
		ArrayList<AccountInfo> results = new ArrayList<>();
		AccountManager accountManager = AccountManager.get(applicationContext);
        for(Account account : accountManager.getAccountsByType(ACCOUNT_TYPE)) {
            results.add(getAccount(applicationContext, account));
        }
		return results;
	}

    public static Account[] getAndroidAccounts(Context applicationContext) {
        AccountManager accountManager = AccountManager.get(applicationContext);
        return accountManager.getAccountsByType(ACCOUNT_TYPE);
    }
}
