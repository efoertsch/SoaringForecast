package org.soaringforecast.rasp.turnpoint.cup;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CupStyles {


    @SerializedName("styles")
    @Expose
    private List<CupStyle> cupStyles = null;

    public List<CupStyle> getCupStyles() {
        return cupStyles;
    }

}
