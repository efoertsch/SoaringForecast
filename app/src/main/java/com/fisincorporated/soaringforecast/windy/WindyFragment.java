package com.fisincorporated.soaringforecast.windy;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.databinding.WindyView;
import com.google.android.gms.maps.model.LatLng;

public class WindyFragment extends Fragment {

    private WindyView windyView;
    private WebView webView;
    private int zoom = 7;

    private LatLng defaultLatLng = new LatLng( 43.1393051,-72.076004);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        windyView = DataBindingUtil.inflate(inflater, R.layout.fragment_windy, container, false);
        webView = windyView.fragmentWindyWebview;
        //webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "android");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);
        }
        // prevent popups and new windows (but do not override the onCreateWindow() )
        webView.getSettings().setSupportMultipleWindows(true);

        webView.loadUrl("file:///android_asset/windy.html");

        return windyView.getRoot();
    }


    @JavascriptInterface
    public String getWindyKey(){
        return getString(R.string.WindyKey);
    }

    @JavascriptInterface
    public double getLat() {
        return defaultLatLng.latitude;
    }

    @JavascriptInterface
    public double getLong(){
        return defaultLatLng.longitude;
    }

    @JavascriptInterface
    public double getZoom(){
        return zoom;
    }

}
