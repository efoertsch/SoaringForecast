package org.soaringforecast.rasp.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import timber.log.Timber;

/**
 * GSON is assigning fields directly and not using setter methods. (Switch to Jackson?)
 * Hence the additional code for lat/lng
 */
public class Sounding {

    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("latitude")
    @Expose
    private String latitude;

    // custom attribute
    private int position;
    private double lat;
    private double lng;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitue) {
        this.longitude = longitue;
        lng = convertToDouble(longitue);
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String lat) {
        this.latitude = lat;
        this.lat = convertToDouble(lat);
    }

    public void setLatLng() {
        lat = convertToDouble(latitude);
        lng = convertToDouble(longitude);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public static double convertToDouble(String textNumber) {
        try {
            return Double.parseDouble(textNumber);
        } catch (NumberFormatException nfe) {
            Timber.e("Error converting text string to Double: %1$s", textNumber);
        }
        return 0d;
    }


}
