package com.example.coolercontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CloudActivity extends AppCompatActivity {
    public WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud);

        webView = (WebView)  findViewById(R.id.web_cloud);
        webView.loadUrl("https://www.google.com");
        //Load Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //Force links and redirects to stay in webview
        webView.setWebViewClient(new WebViewClient());
    }
}