package com.fisincorporated.aviationweather.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

// Part of TypeLocationAndTimes
// http://www.soargbsc.com/rasp/NewEngland/2018-03-30/status.json
public class GpsLocationAndTimes {

    @SerializedName("corners")
    @Expose
    private List<List<Double>> corners = null;
    @SerializedName("center")
    @Expose
    private List<Double> center = null;
    @SerializedName("times")
    @Expose
    private List<String> times = null;

    public List<List<Double>> getCorners() {
        return corners;
    }

    public void setCorners(List<List<Double>> corners) {
        this.corners = corners;
    }

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

}