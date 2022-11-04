package com.example.coolercontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CloudActivity extends AppCompatActivity {
    public WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Display the activity cloud layout
        setContentView(R.layout.activity_cloud);
        //Create a webView window
        webView = (WebView)  findViewById(R.id.web_cloud);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSaveFormData(true);
        webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://us-east-1.quicksight.aws.amazon.com/sn/dashboards/67f51f8a-2870-4fb1-aecc-a871dc181496?directory_alias=joseberlanga&ignore=true&qs-nonce=evg131q2ty3QT4MWlm%2FpOLYmc69uP6G0%2Bc%2BfbxrODcE%3D&ref_=pe_3035110_233161710#");
        //Load Javascript
        webView.setWebChromeClient(new WebChromeClient());

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieSyncManager.getInstance().startSync();


        //Force links and redirects to stay in webview
        webView.setWebViewClient(new WebViewClient());
    }
}