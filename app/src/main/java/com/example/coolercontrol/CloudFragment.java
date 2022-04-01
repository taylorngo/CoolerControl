package com.example.coolercontrol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class CloudFragment extends Fragment {
    public WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Create webView
        View v = inflater.inflate(R.layout.fragment_cloud, container, false);
        webView = (WebView)  v.findViewById(R.id.web_cloud);
        webView.loadUrl("https://www.google.com");

        //Load Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //Force links and redirects to stay in webview
        webView.setWebViewClient(new WebViewClient());

        return v;
    }
}
