package com.fisincorporated.aviationweather.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationAndTimes {

    @SerializedName("rap")
    @Expose
    private LocationTimes rap;
    @SerializedName("nam")
    @Expose
    private LocationTimes nam;
    @SerializedName("gfs")
    @Expose
    private LocationTimes gfs;

    public LocationTimes getRap() {
        return rap;
    }

    public void setRap(LocationTimes rap) {
        this.rap = rap;
    }

    public LocationTimes getNam() {
        return nam;
    }

    public void setNam(LocationTimes nam) {
        this.nam = nam;
    }

    public LocationTimes getGfs() {
        return gfs;
    }

    public void setGfs(LocationTimes gfs) {
        this.gfs = gfs;
    }

}