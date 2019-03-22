package org.soaringforecast.rasp.messages;

import com.google.android.gms.maps.model.LatLng;

public class DisplayPointForecast {

    private LatLng latLng;

    public DisplayPointForecast(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
