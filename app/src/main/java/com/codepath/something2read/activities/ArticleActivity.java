package com.codepath.something2read.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.codepath.something2read.R;
import com.codepath.something2read.models.Article;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleActivity extends Activity {
    @BindView(R.id.wvArticle) WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_acticle);

        ButterKnife.bind(this);

        Article article = (Article) getIntent().getSerializableExtra("article");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        mWebView.loadUrl(article.getWebUrl());
    }
}
