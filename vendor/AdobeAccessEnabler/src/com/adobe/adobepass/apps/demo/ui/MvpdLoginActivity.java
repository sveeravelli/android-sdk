/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 *
 * For the avoidance of doubt, this file is Documentation under the Agreement.
 ************************************************************************/

package com.adobe.adobepass.apps.demo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.adobe.adobepass.accessenabler.api.AccessEnabler;
import com.adobe.adobepass.accessenabler.utils.Log;
import com.adobe.adobepass.apps.demo.AdobePassDemoApp;
import com.adobe.adobepass.apps.demo.R;

import java.net.URLDecoder;

public class MvpdLoginActivity extends AbstractActivity {
    private static final String LOG_TAG = "MvpdLoginActivity";

    private WebView loginWebView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // retrieve the name of the MVPD from the intent that started the activity
        Intent intent = getIntent();
        String targetUrl = intent.getStringExtra("url");

        // inflate the layout for this activity
        setContentView(R.layout.mvpd_login);

        // get references to UI elements
        loginWebView = (WebView) findViewById(R.id.mvpd_login_webview);

        // install listeners for various page load events
        loginWebView.setWebViewClient(webViewClient);

        // enable JavaScript support
        WebSettings browserSettings = loginWebView.getSettings();
        browserSettings.setJavaScriptEnabled(true);
        browserSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // navigate to target URL
        Log.d(LOG_TAG, "Loading: " + targetUrl);
        loginWebView.loadUrl(targetUrl);

        // update the title bar with the client version
        AccessEnabler accessEnabler = AdobePassDemoApp.getAccessEnablerInstance();
        setTitle(getResources().getString(R.string.app_name) + " (v" + accessEnabler.getVersion() + ")");
    }

    private final WebViewClient webViewClient = new WebViewClient() {
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            Log.d(LOG_TAG, "Loading URL: " + url);

            // if we detect a redirect to our application URL, this is an indication
            // that the authN workflow was completed successfully
            if (url.equals(URLDecoder.decode(AccessEnabler.ADOBEPASS_REDIRECT_URL))) {

                // the authentication workflow is now complete - go back to the main activity
                Intent result = new Intent(MvpdLoginActivity.this, MainActivity.class);
                setResult(RESULT_OK, result);
                finish();
            }

            return false;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.d(LOG_TAG, "Ignoring SSL certificate error.");
            handler.proceed();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(LOG_TAG, description);
            Log.d(LOG_TAG, failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(LOG_TAG, "Page started: " + url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(LOG_TAG, "Page loaded: " + url);
            super.onPageFinished(view, url);
        }
    };
}
