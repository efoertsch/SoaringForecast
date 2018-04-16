package com.fisincorporated.aviationweather.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


//http://www.soargbsc.com/rasp/NewEngland/2018-03-30/status.json
// Run for each date from RegionForecastDates
public class TypeLocationAndTimes {

    @SerializedName("rap")
    @Expose
    private GpsLocationAndTimes rap;
    @SerializedName("nam")
    @Expose
    private GpsLocationAndTimes nam;
    @SerializedName("gfs")
    @Expose
    private GpsLocationAndTimes gfs;

    public GpsLocationAndTimes getRap() {
        return rap;
    }

    public void setRap(GpsLocationAndTimes rap) {
        this.rap = rap;
    }

    public GpsLocationAndTimes getNam() {
        return nam;
    }

    public void setNam(GpsLocationAndTimes nam) {
        this.nam = nam;
    }

    public GpsLocationAndTimes getGfs() {
        return gfs;
    }

    public void setGfs(GpsLocationAndTimes gfs) {
        this.gfs = gfs;
    }

}