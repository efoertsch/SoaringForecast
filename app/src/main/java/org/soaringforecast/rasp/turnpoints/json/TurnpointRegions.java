package org.soaringforecast.rasp.turnpoints.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TurnpointRegions {


    @SerializedName("turnpointregions")
    @Expose
    private List<TurnpointRegion> turnpointRegions = null;

    public List<TurnpointRegion> getTurnpointRegions() {
        return turnpointRegions;
    }

    public void setTurnpointRegions(List<TurnpointRegion> turnpointRegions) {
        this.turnpointRegions = turnpointRegions;
    }

}