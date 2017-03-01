package com.fisincorporated.metar;

import android.support.test.espresso.IdlingResource;

import com.fisincorporated.aviationweather.airportweather.AirportWeatherActivity;

public class MetarActivityIdlingResource implements IdlingResource {

    private AirportWeatherActivity activity;
    private ResourceCallback callback;

    public MetarActivityIdlingResource(AirportWeatherActivity activity) {
        this.activity = activity;
    }

    @Override
    public String getName() {
        return "MetarActivityIdleName";
    }

    @Override
    public boolean isIdleNow() {
        Boolean idle = isIdle();
        if (idle) callback.onTransitionToIdle();
        return idle;
    }

    public boolean isIdle() {
       // return activity != null && callback != null && activity.isSyncFinished();
        return true;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.callback = resourceCallback;
    }
}
