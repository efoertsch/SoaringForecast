package org.soaringforecast.rasp.soaring.messages;

import com.google.android.gms.maps.model.LatLng;

public class DisplayLatLngForecast {

    private LatLng latLng;

    public DisplayLatLngForecast(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
