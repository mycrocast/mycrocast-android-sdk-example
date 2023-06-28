package de.mycrocast.android.sdk.example.advertisement;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import de.mycrocast.android.sdk.example.R;

public class WebViewActivity extends AppCompatActivity {

    private static final String URL_KEY = "url_to_open";

    /**
     * Creates a new intent to start a new WebViewActivity, that will display the website of the given url.
     *
     * @param context
     * @param urlToOpen the url of the website, that should be displayed
     * @return Intent for starting a WebViewActivity
     */
    public static Intent newInstance(Context context, String urlToOpen) {
        Intent result = new Intent(context, WebViewActivity.class);
        result.putExtra(URL_KEY, urlToOpen);
        return result;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_web_view);

        String urlToOpen = this.getIntent().getStringExtra(URL_KEY);
        if (urlToOpen == null || urlToOpen.isEmpty()) {
            this.finish();
            return;
        }

        WebView webView = this.findViewById(R.id.webView);
        webView.loadUrl(urlToOpen);
    }
}