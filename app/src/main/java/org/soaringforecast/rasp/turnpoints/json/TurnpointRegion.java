package org.soaringforecast.rasp.turnpoints.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TurnpointRegion {

    @SerializedName("region")
    @Expose
    private String region;
    @SerializedName("turnpointfiles")
    @Expose
    private List<TurnpointFile> turnpointFiles = null;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<TurnpointFile> getTurnpointFiles() {
        return turnpointFiles;
    }

    public void setTurnpointFiles(List<TurnpointFile> turnpointFiles) {
        this.turnpointFiles = turnpointFiles;
    }

}

