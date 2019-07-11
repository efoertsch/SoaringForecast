package org.soaringforecast.rasp.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SUAGeoJsonFile {

    @SerializedName("region")
    @Expose
    private String region;

    @SerializedName("sua_file_name")
    @Expose
    private String suaFileName;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSuaFileName() {
        return suaFileName;
    }

    public void setSuaFileName(String suaFileName) {
        this.suaFileName = suaFileName;
    }

}