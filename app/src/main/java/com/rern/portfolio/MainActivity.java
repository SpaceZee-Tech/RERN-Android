package com.rern.portfolio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    private static final String APP_URL = "https://rern.pythonanywhere.com";
    private static final int FILE_CHOOSER_REQUEST = 100;

    private WebView webView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout errorLayout;
    private ValueCallback<Uri[]> filePathCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        errorLayout = findViewById(R.id.errorLayout);
        Button retryButton = findViewById(R.id.retryButton);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                CookieManager.getInstance().flush();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("rern.pythonanywhere.com")) {
                    return false;
                } else if (url.startsWith("mailto:")) {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                    return true;
                } else if (url.startsWith("tel:")) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                    return true;
                } else if (url.contains("instagram.com")) {
                    // Open Instagram links in browser/app
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (!isNetworkAvailable()) {
                    errorLayout.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback,
                                              FileChooserParams fileChooserParams) {
                filePathCallback = callback;
                try {
                    startActivityForResult(fileChooserParams.createIntent(), FILE_CHOOSER_REQUEST);
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        swipeRefreshLayout.setColorSchemeColors(0xFFE8DCC8);
        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        retryButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                errorLayout.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                webView.reload();
            }
        });

        if (isNetworkAvailable()) {
            webView.loadUrl(APP_URL);
        } else {
            errorLayout.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            filePathCallback.onReceiveValue(
                WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            filePathCallback = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override protected void onResume() { super.onResume(); webView.onResume(); }
    @Override protected void onPause() { super.onPause(); webView.onPause(); }
    @Override protected void onDestroy() { if (webView != null) webView.destroy(); super.onDestroy(); }
}
