package org.soaringforecast.rasp.soaring.forecast;

import com.google.android.gms.maps.model.LatLng;

public class LatLngForecast {
    private final LatLng latLng;
    private final String forecastText;

    public LatLngForecast(LatLng latLng, String forecastText) {
        this.latLng = latLng;
        this.forecastText = forecastText;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getForecastText() {
        return forecastText;
    }
}
