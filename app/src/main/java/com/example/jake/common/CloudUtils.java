package com.example.jake.common;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.os.Environment;

import com.example.jake.basesyncadapter.CloudProviders;
import com.example.jake.common.accounts.AccountInfo;
import com.example.jake.googledrive.DriveCloudStorage;
import com.example.jake.googledrive.DriveUserInfo;
import com.example.jake.googledrive.DriveUtils;


public class CloudUtils {
	public static AccountInfo getAccountInfo(CloudProviders cloudProvider, String accessToken) {
		AccountInfo accountInfo = new AccountInfo();
		accountInfo.AccessToken = accessToken;
        accountInfo.Provider = cloudProvider;
		switch (cloudProvider) {
		case GoogleDrive:
			DriveUserInfo driveUserInfo = DriveUtils.getUserInfo(accessToken);
			if (driveUserInfo != null) {
				accountInfo.AccountId = driveUserInfo.sub;
				accountInfo.AccountName = driveUserInfo.email;
				return accountInfo;
			}
			break;
		}
		return null;
	}

	public static IReadableCloudStorage getCloudStorage(Context context,
			CloudProviders cloudProvider) {
		IReadableCloudStorage result = null;
		switch (cloudProvider) {
		case GoogleDrive:
			result = new DriveCloudStorage(context);
			break;
		default:
			break;
		}

		return result;
	}

	public static String getAppTempDownloadedDirectory() {
		File directory = new File(Environment.DIRECTORY_DOWNLOADS + File.separator + "HisenseFileManager");
		if (!directory.exists()) {
			try {
				directory.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return directory.getPath();
	}

	private static volatile HttpClient HTTP_CLIENT;
	private static Object HTTP_CLIENT_LOCK = new Object();

	public static int getSocketTimeout() {
		return mSocketTimeout;
	}

	public static void setSocketTimeout(int socketTimeoutInMS) {
		mSocketTimeout = socketTimeoutInMS;
	}

	public static final int DEFAULT_SOCKET_TIMEOUT_IN_MS = 10 * 1000;
	private static int mSocketTimeout = DEFAULT_SOCKET_TIMEOUT_IN_MS;

	public static int getConnectTimeout() {
		return mConnectTimeout;
	}

	public static void setConnectTimeout(int connectTimeoutInMS) {
		mConnectTimeout = connectTimeoutInMS;
	}

	public static int DEFAULT_CONNECT_TIMEOUT_IN_MS = 10 * 1000;
	private static int mConnectTimeout = DEFAULT_CONNECT_TIMEOUT_IN_MS;

	public static HttpClient getHttpClient() {
		if (HTTP_CLIENT == null) {
			synchronized (HTTP_CLIENT_LOCK) {
				if (HTTP_CLIENT == null) {
					HttpParams params = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(params, getConnectTimeout());
					HttpConnectionParams.setSoTimeout(params, getSocketTimeout());

					ConnManagerParams.setMaxTotalConnections(params, 100);
					HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

					SchemeRegistry schemeRegistry = new SchemeRegistry();
					schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
					schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

					// Create an HttpClient with the
					// ThreadSafeClientConnManager.
					// This connection manager must be used if more than one
					// thread will
					// be using the HttpClient, which is a common scenario.
					ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
					HTTP_CLIENT = new DefaultHttpClient(cm, params);
				}
			}
		}
		HttpConnectionParams.setConnectionTimeout(HTTP_CLIENT.getParams(), getConnectTimeout());
		HttpConnectionParams.setSoTimeout(HTTP_CLIENT.getParams(), getSocketTimeout());
		return HTTP_CLIENT;
	}
}
