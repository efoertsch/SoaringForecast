package com.fisincorporated.soaringforecast.windy;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.webkit.JavascriptInterface;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.google.android.gms.maps.model.LatLng;

public class WindyViewModel extends AndroidViewModel {

    Context context;
    MutableLiveData<String> command;

    // TODO put zoom in appPreferences
    private int zoom = 7;
    private LatLng defaultLatLng = new LatLng(43.1393051, -72.076004);
    private AppPreferences appPreferences;

    public WindyViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

     WindyViewModel setAppPreferences(AppPreferences appPreferences){
        this.appPreferences =  appPreferences;
        return this;
    }

    public MutableLiveData<String> getCommand() {
        if (command == null) {
            command = new MutableLiveData<>();
        }
        return command;
    }

    @JavascriptInterface
    public void resetHeight() {
        command.setValue("javascript:setHeight('200px')");
    }

    public void drawLine(double fromLat, double fromLong, double toLat, double toLong) {
        command.setValue("javascript:drawLine( " + fromLat + "," + fromLong
                + "," + toLat + "," + toLong + ")");
    }

    @JavascriptInterface
    public String getWindyKey() {
        return context.getString(R.string.WindyKey);
    }

    @JavascriptInterface
    public double getLat() {
        return defaultLatLng.latitude;
    }

    @JavascriptInterface
    public double getLong() {
        return defaultLatLng.longitude;
    }

    @JavascriptInterface
    public double getZoom() {
        return appPreferences.getWindyZoomLevel();
    }

}
