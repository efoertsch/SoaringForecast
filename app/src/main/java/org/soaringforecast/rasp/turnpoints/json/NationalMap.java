package org.soaringforecast.rasp.turnpoints.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NationalMap {

    @SerializedName("USGS_Elevation_Point_Query_Service")
    @Expose
    private USGSElevationPointQueryService usgsElevationPointQueryService;

    public USGSElevationPointQueryService getUSGSElevationPointQueryService() {
        return usgsElevationPointQueryService;
    }

    public void setUSGSElevationPointQueryService(USGSElevationPointQueryService usgsElevationPointQueryService) {
        this.usgsElevationPointQueryService = usgsElevationPointQueryService;
    }

}