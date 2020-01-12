package org.soaringforecast.rasp.turnpoints.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * See
 *  https://nationalmap.gov/epqs/
 *  Example call: https://nationalmap.gov/epqs/pqs.php?x=-71.791&y=42.425&units=Feet&output=json
 * response: {"USGSElevationPointQueryService":{"ElevationQuery":{"x":-71.791,"y":42.425,"Data_Source":"3DEP 1\/3 arc-second","Elevation":459.92,"Units":"Feet"}}}
 * Lat/long must be in decimal degrees
 * Note parm x is longitude, y is latitude  W longitude, S latitude must be negative
 */
public class USGSElevationPointQueryService {

    @SerializedName("Elevation_Query")
    @Expose
    private ElevationQuery elevation_Query;

    public ElevationQuery getElevationQuery() {
        return elevation_Query;
    }

    public void setElevationQuery(ElevationQuery elevation_Query) {
        this.elevation_Query = elevation_Query;
    }

}