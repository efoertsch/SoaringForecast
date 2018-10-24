package com.fisincorporated.soaringforecast.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Soundings {

    @SerializedName("region")
    @Expose
    private String region;
    @SerializedName("locations")
    @Expose
    private List<SoundingLocation> soundingLocations = null;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<SoundingLocation> getSoundingLocations() {
        return soundingLocations;
    }

    public void setSoundingLocations(List<SoundingLocation> locations) {
        this.soundingLocations = locations;
    }

}

