package org.soaringforecast.rasp.turnpoint.cup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CupStyle {

    @SerializedName("style")
    @Expose
    private String style;   // "0"

    @SerializedName("description")
    @Expose
    private String description;    //"Unknown"

    public String getStyle() {
        return style;
    }

    public String getDescription() {
        return description;
    }
}
