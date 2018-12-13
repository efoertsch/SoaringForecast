package com.fisincorporated.soaringforecast.windy;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.databinding.WindyView;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.support.DaggerFragment;

public class WindyFragment extends DaggerFragment {

    @Inject
    @Named("appWindyUrl")
    String appWindyUrl;

    @Inject
    AppPreferences appPreferences;

    private WindyViewModel windyViewModel;
    private WindyView windyView;
    private WebView webView;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        windyViewModel = ViewModelProviders.of(this)
                .get(WindyViewModel.class)
                .setAppPreferences(appPreferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        windyView = DataBindingUtil.inflate(inflater, R.layout.fragment_windy, container, false);
        setupViews();
        setObservers();

        return windyView.getRoot();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupViews() {
        webView = windyView.fragmentWindyWebview;
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().equals(appWindyUrl)) {
                    // This is app related url so ignore
                    return false;
                }
                // Otherwise, start browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                // resetHeight();
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(windyViewModel, "android");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // prevent popups and new windows (but do not override the onCreateWindow() )
        webView.getSettings().setSupportMultipleWindows(true);

        Button testButton = windyView.fragmentWindyTestButton;
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawLine(43.1393051, -72.076004, 43.5, -72.84);
            }
        });


        webView.loadUrl(appWindyUrl);
    }

    public void drawLine(double fromLat, double fromLong, double toLat, double toLong) {
       executeJavaScriptCommand("javascript:drawLine( " + fromLat + "," + fromLong
                + "," + toLat + "," + toLong + ")");
    }

    public void setObservers() {
        windyViewModel.getCommand().observe(this, command -> {
            // execute javascript command
            executeJavaScriptCommand(command);
        });
    }

    private void executeJavaScriptCommand(String command) {
        webView.evaluateJavascript(command, null);
    }

}
