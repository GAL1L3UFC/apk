package nemosofts.online.live.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.nemosofts.AppCompat;
import androidx.nemosofts.AppCompatActivity;

import java.util.Objects;

import nemosofts.online.live.R;

import nemosofts.online.live.utils.IfSupported;
import nemosofts.online.live.utils.helper.Helper;

public class WebActivity extends AppCompatActivity {

    private Helper helper;
    private WebView webView;
    private FrameLayout frameLayout;
    private String webWRL = "";

    OnBackPressedCallback callback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IfSupported.IsRTL(this);
        IfSupported.IsScreenshot(this);

        Intent intent = getIntent();
        webWRL = intent.getStringExtra("web_url");
        String page_title = intent.getStringExtra("page_title");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> callback.handleOnBackPressed());
        setTitle(page_title);

        helper = new Helper(this);

        final ProgressBar pb = findViewById(R.id.pb_web);
        frameLayout = findViewById(R.id.fl_empty);
        webView = findViewById(R.id.web);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                pb.setProgress(progress);
                if (progress == 100) {
                    pb.setVisibility(View.GONE);
                } else {
                    pb.setVisibility(View.VISIBLE);
                }
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        loadData();

        LinearLayout adView = findViewById(R.id.ll_adView);
        new Helper(this).showBannerAd(adView,"");

        getOnBackPressedDispatcher().addCallback(this, callback);
        callback.setEnabled(true);
    }

    private void loadData() {
        if (helper.isNetworkAvailable()) {
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(Objects.requireNonNull(webWRL));
            frameLayout.setVisibility(View.GONE);
        } else {
            webView.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View myView = inflater.inflate(R.layout.layout_empty, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(getString(R.string.error_internet_not_connected));

            myView.findViewById(R.id.ll_empty_try).setOnClickListener(v -> loadData());

            frameLayout.addView(myView);
        }
    }

    private static class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(@NonNull WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public int setLayoutResourceId() {
        return R.layout.activity_web;
    }

    @Override
    public int setAppCompat() {
        return AppCompat.COMPAT();
    }
}