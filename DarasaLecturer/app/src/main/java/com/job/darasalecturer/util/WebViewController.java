package com.job.darasalecturer.util;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.view.View;


public class WebViewController extends WebViewClient {

    private ProgressBar progressBar;

    public WebViewController(ProgressBar progressBar) {
        this.progressBar = progressBar;
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);


        progressBar.setVisibility(View.GONE);
    }

    public void showLoader(){

    }
}
