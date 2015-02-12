package com.example.jake.common.oauth2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.jake.basesyncadapter.CloudProviders;
import com.example.jake.basesyncadapter.R;
import com.example.jake.common.CloudUtils;
import com.example.jake.common.accounts.AccountInfo;
import com.example.jake.common.accounts.AddAccountActivity;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.util.Locale;


public class OAuth2Activity extends Activity {

    private String _url;
    private String _tokenServerUrl;
    private String _clientId;
    private String _clientSecret;
    private String _redirectUri;
    private String _loggingInMessage;

    private String _loginSuccessfulMessage;
    private String _loginFailedMessage;
    private int mCloudProvider;

    private ProgressDialog _loadingDialog;
    private ProgressDialog _progressDialog;

    private Boolean finished = false;
    private Intent mIntent;

    public OAuth2Activity() {
        super();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setTheme(android.R.style.Theme_Holo_NoActionBar);
        if (getIntent() != null) {
            mIntent = getIntent();
        }
        loadWebView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null) {
            mIntent = intent;
        }
        super.onNewIntent(intent);
    }

    private void loadWebView() {
        if (mIntent == null || mIntent.getExtras() == null) {
            finish();
            return;
        }
        _url = mIntent.getStringExtra("loginUrl");
        _tokenServerUrl = mIntent.getExtras().getString("tokenServerUrl");
        _clientId = mIntent.getExtras().getString("clientId");
        _clientSecret = mIntent.getExtras().getString("clientSecret");
        _redirectUri = mIntent.getExtras().getString("redirectUri");
        _loggingInMessage = getResources().getString(R.string.logging_in);
        _loginSuccessfulMessage = getResources().getString(R.string.log_in_successful);
        _loginFailedMessage = getResources().getString(R.string.log_in_failed);
        mCloudProvider = mIntent.getExtras().getInt("cloudProvider", 0);

        _loadingDialog = new ProgressDialog(OAuth2Activity.this);
        _progressDialog = new ProgressDialog(OAuth2Activity.this);
        _progressDialog.setMessage(new String(_loggingInMessage + "..."));
        _loadingDialog.setMax(100);
        _loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        _loadingDialog.show();

        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setBackgroundColor(getResources().getColor(android.R.color.white));
        relativeLayout
                .setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        final WebView webView = new WebView(this) {
        };
        webView.setScrollContainer(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (_loadingDialog != null) {
                    _loadingDialog.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                    HitTestResult result = view.getHitTestResult();
                    if (result != null && result.getType() == HitTestResult.EDIT_TEXT_TYPE) {
                        inputMethodManager.showSoftInput(view, 0);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onPageFinished(final WebView view, final String url) {
                if (_loadingDialog != null) {
                    _loadingDialog.dismiss();
                    _loadingDialog = null;
                }

                String title = view.getTitle();
                if (null != title && title.toLowerCase(Locale.ENGLISH).contains("access_denied")) {
                    finish();
                    return;
                }
                processInfo("", title);
            }

            private boolean processInfo(String url, String title) {
                try {
                    if (OAuth2Activity.this == null) {
                        return false;
                    }
                    String code = "";
                    Uri uri = null;
                    try {
                        uri = Uri.parse(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (uri != null && uri.getQueryParameter("code") != null) {
                        code = uri.getQueryParameter("code");
                    } else if (title != null && title.toLowerCase(Locale.ENGLISH).contains("code=")) {
                        String[] titleParts = title.split("code=");
                        code = titleParts[1];
                    }
                    if (code.length() > 0) {
                        _progressDialog.show();

                        webView.setVisibility(WebView.INVISIBLE);
                        if (!finished) {
                            finished = true;
                            final String authCode = code;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    synchronized (OAuth2Activity.this) {
                                        boolean signInFailed = false;
                                        String statusMessage;
                                        AccountInfo accountInfo = null;
                                        try {
                                            OAuth2AuthorizationCodeFlow flow = new OAuth2AuthorizationCodeFlow.Builder(
                                                    _tokenServerUrl, "", new NetHttpTransport(),
                                                    new GsonFactory(), _clientId, _clientSecret, null)
                                                    .setAccessType("offline").setApprovalPrompt("auto")
                                                    .build();

                                            AuthorizationCodeTokenRequest request = flow.newTokenRequest(
                                                    authCode).setRedirectUri(_redirectUri);
                                            TokenResponse response = request.execute();

                                            if (mCloudProvider == CloudProviders.GoogleDrive.ordinal()) {
                                                accountInfo = CloudUtils.getAccountInfo(
                                                        CloudProviders.GoogleDrive, response.getAccessToken());

                                            }

                                            statusMessage = _loginSuccessfulMessage;
                                        } catch (Exception e) {
                                            signInFailed = true;
                                            if (e.getMessage() != null) {
                                                statusMessage = _loginFailedMessage + ": " + e.getMessage();
                                            } else {
                                                statusMessage = _loginFailedMessage;
                                            }
                                            e.printStackTrace();
                                        }
                                        final String status = statusMessage;

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (_progressDialog.isShowing()) {
                                                    _progressDialog.dismiss();
                                                }
                                                Toast.makeText(getApplicationContext(), status,
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });

                                        if (!signInFailed) {
                                            Intent data = new Intent();
                                            data.putExtra(AddAccountActivity.EXTRA_ACCOUNT_NAME, accountInfo.AccountName);
                                            data.putExtra(AddAccountActivity.EXTRA_ACCOUNT_ID, accountInfo.AccountId);
                                            data.putExtra(AddAccountActivity.EXTRA_ACCESS_TOKEN, accountInfo.AccessToken);
                                            data.putExtra(AddAccountActivity.EXTRA_REFRESH_TOKEN, accountInfo.RefreshToken);
                                            data.putExtra(AddAccountActivity.EXTRA_PROVIDER, accountInfo.Provider);;
                                            setResult(Activity.RESULT_OK, data);
                                        } else {
                                            setResult(AddAccountActivity.AUTHENTICATION_FAILED);
                                        }
                                        finish();
                                    }
                                }
                            }).start();
                            String html = "<html><body>Processing...</body></html>";
                            String mime = "text/html";
                            String encoding = "utf-8";

                            webView.loadDataWithBaseURL(null, html, mime, encoding, null);
                            return true;
                        }
                    }
                } catch (Exception e) {

                }
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (failingUrl.startsWith("mailto:")) {
                    try {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(failingUrl));
                        startActivity(Intent.createChooser(emailIntent, "Send email"));
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), _loginFailedMessage + ": " + description,
                            Toast.LENGTH_LONG).show();
                    finish();
                    super.onReceivedError(view, errorCode, description, failingUrl);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                if (url.toLowerCase(Locale.ENGLISH).contains("access_denied")) {
                    finish();
                    return true;
                }
                String title = view.getTitle();
                return processInfo(url, title);
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSavePassword(false);

        webView.loadUrl(_url);
        // webView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        // LayoutParams.MATCH_PARENT));
        webView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        relativeLayout.addView(webView);
        webView.setVisibility(View.VISIBLE);

        setContentView(relativeLayout);
    }

    @Override
    public void finish() {
        CookieSyncManager.createInstance(getApplicationContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        super.finish();
    }
}
