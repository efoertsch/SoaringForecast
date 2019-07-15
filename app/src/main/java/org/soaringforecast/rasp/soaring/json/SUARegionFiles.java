package org.soaringforecast.rasp.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SUARegionFiles {

    @SerializedName("sua_regions")
    @Expose
    private List<SUARegion> suaRegionList = null;

    public List<SUARegion> getSuaRegionList() {
        return suaRegionList;
    }

    public void setSuaRegionList(List<SUARegion> suaRegionList) {
        this.suaRegionList = suaRegionList;
    }

}