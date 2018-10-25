package com.fisincorporated.soaringforecast.task.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TurnpointFiles {

    @SerializedName("regions")
    @Expose
    private List<Region> regions = null;

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

}