package com.example.ex5_clientservercommunication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class BookingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Get data from MainActivity
        String service = getIntent().getStringExtra("service");
        String url = getIntent().getStringExtra("url");

        // Get views from layout
        WebView webView = findViewById(R.id.webView);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvStatus = findViewById(R.id.tvStatus);

        // Set title and status
        tvTitle.setText(service + " Booking");
        tvStatus.setText("Connecting...");

        // Setup WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        // Handle page loading
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                tvStatus.setText("Loading...");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                tvStatus.setText("✅ Connected");
                Toast.makeText(BookingActivity.this,
                        "Connected to " + service + " server",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Show loading progress
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });

        // Load the website
        webView.loadUrl(url);
    }

    // Handle back button
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        WebView webView = findViewById(R.id.webView);
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}