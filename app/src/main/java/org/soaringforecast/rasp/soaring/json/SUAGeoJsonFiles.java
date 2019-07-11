package org.soaringforecast.rasp.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SUAGeoJsonFiles {

    @SerializedName("sua_files")
    @Expose
    private List<SUAGeoJsonFile> suaGeoJsonfiles = null;

    public List<SUAGeoJsonFile> getSuaGeoJsonFiles() {
        return suaGeoJsonfiles;
    }

    public void setSuaGeoJsonfiles(List<SUAGeoJsonFile> suaGeoJsonfiles) {
        this.suaGeoJsonfiles = suaGeoJsonfiles;
    }

}