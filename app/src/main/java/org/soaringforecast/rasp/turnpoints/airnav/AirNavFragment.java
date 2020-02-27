package org.soaringforecast.rasp.turnpoints.airnav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.soaringforecast.rasp.R;

import dagger.android.support.DaggerFragment;

public class AirNavFragment extends DaggerFragment {

    private static final String AIRPORT_CODE = "AIRPORT_CODE";
    private String airportCode;


    public static AirNavFragment newInstance(String airportCode) {
        AirNavFragment airNavFragment = new AirNavFragment();
        Bundle bundle = new Bundle();
        bundle.putString(AIRPORT_CODE, airportCode);
        airNavFragment.setArguments(bundle);
        return airNavFragment;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        airportCode = getArguments().getString(AIRPORT_CODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.airnav_fragment, container, false);
        WebView airNavWebView = (WebView) view.findViewById(R.id.fragment_airnav_webview);
        airNavWebView.loadUrl("http://www.airnav.com/airport/" + airportCode);
        return view;
    }

}
