package com.fisincorporated.aviationweather.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


// Json { "NewEngland" : [ [ "Friday March 30", "2018-03-30" ], [ "Saturday March 31", "2018-03-31" ], ... ] } “
public class ForecastDates {

    @SerializedName("NewEngland")
    @Expose
    private List<List<String>> forecastDates = null;

    public List<List<String>> getForecastDates() {
        return forecastDates;
    }

    public void setForecastDates(List<List<String>> forecastDates) {
        this.forecastDates = forecastDates;
    }

}