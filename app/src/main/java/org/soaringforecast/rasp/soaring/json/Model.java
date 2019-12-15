package org.soaringforecast.rasp.soaring.json;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;


/**
 * Contains the specifics for a particular forecast model (GFS, NAM) and date.
 * Name field contains the model name
 */
public class Model {

    // Lat/Long gps coordinates of center of forecast area
    @SerializedName("center")
    @Expose
    private List<Double> center = null;

    // Times for which forecast created
    @SerializedName("times")
    @Expose
    private List<String> times = null;

    // Name of forecast model - GFS, NAM, RAP, ...
    @SerializedName("name")
    @Expose
    private String name;

    // Lat/Long gps coordinates of forecast area (Southwest and NorthEast corners)
    @SerializedName("corners")
    @Expose
    private List<List<Double>> corners = null;

    public List<Double> getCenter() {
        return center;
    }

    public void setCenter(List<Double> center) {
        this.center = center;
    }

    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<List<Double>> getCorners() {
        return corners;
    }

    public void setCorners(List<List<Double>> corners) {
        this.corners = corners;
    }

    // ------ Custom code --------------------
    public LatLng getSouthWestLatLng(){
        if (corners != null && corners.size() > 0 && corners.get(0).size() > 1) {
            return new LatLng(corners.get(0).get(0), corners.get(0).get(1));
        }
        return new LatLng(0.0d, 0.0d);
    }

    public LatLng getNorthEastLatLng(){
        if (corners != null && corners.size() > 1 && corners.get(1).size() > 1) {
            return new LatLng(corners.get(1).get(0), corners.get(1).get(1));
        }
        return new LatLng(0.0d, 0.0d);
    }

    @Override
    public String toString() {
        return "center:" + Arrays.toString(center.toArray()) +
                " times:" + Arrays.toString(times.toArray()) +
                " name:" + name +
                " corners:" + Arrays.toString(corners.toArray());
    }

}