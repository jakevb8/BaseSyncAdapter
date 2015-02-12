package com.example.jake.common.oauth2;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.example.jake.basesyncadapter.CloudProviders;
import com.example.jake.common.accounts.AccountInfo;
import com.example.jake.common.accounts.AddAccountActivity;
import com.example.jake.common.accounts.CloudServiceAccountUtils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OAuth2Utils {
    public static void authenticate(Context context, Intent acitivtyIntent, final String tokenServerUrl,
                                    final String authServerUrl,
                                    final CloudProviders cloudProvider, final String clientId, final String clientSecret,
                                    List<String> scopes, final String redirectUri) {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        OAuth2AuthorizationCodeFlow flow = new OAuth2AuthorizationCodeFlow.Builder(tokenServerUrl,
                authServerUrl, httpTransport, jsonFactory, clientId, clientSecret, scopes)
                .setAccessType("offline").setApprovalPrompt("auto").build();

        final String loginUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri)
                .setResponseTypes(Arrays.asList("code")).build();

        acitivtyIntent.putExtra("loginUrl", loginUrl);
        acitivtyIntent.putExtra("tokenServerUrl", tokenServerUrl);
        acitivtyIntent.putExtra("clientId", clientId);
        acitivtyIntent.putExtra("clientSecret", clientSecret);
        acitivtyIntent.putExtra("redirectUri", redirectUri);
        acitivtyIntent.putExtra("cloudProvider", cloudProvider.ordinal());

        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            activity.startActivityForResult(acitivtyIntent, AddAccountActivity.AUTHENTICATION_COMPLETED);
        } else if (context instanceof Application) {
            Application application = (Application) context;
            acitivtyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            application.startActivity(acitivtyIntent);
        }
    }

    public static AccountInfo refreshAccessToken(Context context, AccountInfo accountInfo,
                                                 String tokenServerUrl, String clientId, String clientSecret) {
        OAuth2RefreshTokenRequest request = new OAuth2RefreshTokenRequest(tokenServerUrl,
                new NetHttpTransport(), new GsonFactory(), accountInfo.RefreshToken, clientId, clientSecret);
        try {
            OAuth2TokenResponse response = request.execute();
            CloudServiceAccountUtils.updateTokens(context, accountInfo, response.getAccessToken(), response.getRefreshToken());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return accountInfo;
    }
}
