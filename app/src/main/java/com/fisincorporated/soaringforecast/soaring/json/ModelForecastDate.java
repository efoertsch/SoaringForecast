package com.fisincorporated.soaringforecast.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * ModelForecastDate will contain everything needed to construct the url for the forecast bitmap and map the results
 */
public class ModelForecastDate  {
    @SerializedName("index")
    @Expose
    private int index;

    @SerializedName("regionName")
    @Expose
    private String regionName;    // "NewEngland"

    @SerializedName("model")
    @Expose
    private Model model;          // GFS, times, center, corners

    @SerializedName("printDate")
    @Expose
    private String printDate;    //"Friday March 30"

    @SerializedName("date")
    @Expose
    private String date;        //"2018-03-30"

    public ModelForecastDate(int index, String regionName,Model model, String printDate, String date) {
       this.index = index;
       this.regionName = regionName;
        this.model = model;
        this.printDate = printDate;
        this.date = date;
    }

    public int getIndex() {
        return index;
    }

    public Model getModel() {
        return model;
    }

    public String getPrintDate() {
        return printDate;
    }

    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelForecastDate) {
            ModelForecastDate c = (ModelForecastDate) obj;
            return c.model.equals(model) && c.printDate.equals(printDate);
        }
        return false;
    }

    public String getRegionName() {
        return regionName;
    }

    // For spinner date display
    @Override
    public String toString() {
        return printDate;
    }



}
