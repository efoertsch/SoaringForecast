package com.fisincorporated.aviationweather.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


//GET  “current.json?(time in millisecs)
//    e.g.  http://www.soargbsc.com/rasp/current.json?123456
// retrives
// { "NewEngland" : [ [ "Friday March 30", "2018-03-30" ], [ "Saturday March 31", "2018-03-31" ], ... ] } “
// You then need to parse out the formatted(Friday March 30) and yyyy-mm-dd (dates
public class RegionForecastDates {

    // parsed json dates
    private ArrayList<RegionForecastDate> forecastDates = new ArrayList<>();

    @SerializedName("NewEngland")
    @Expose
    private List<List<String>> stringDateList = null;

    public List<List<String>> getStringDateList() {
        return stringDateList;
    }

    public void setStringDateList(List<List<String>> stringDateList) {
        this.stringDateList = stringDateList;
    }

    public List<RegionForecastDate> getRegionForecastDateList() {
        return forecastDates;
    }

    public void parseForecastDates() {
        forecastDates.clear();
        if (stringDateList != null) {
            for (List<String> dates : stringDateList) {
                if (dates.size() == 2) {
                    RegionForecastDate forecastDate = new RegionForecastDate(forecastDates.size(), dates.get(0), dates.get(1));
                    forecastDates.add(forecastDate);
                }
            }
        }
    }

    public ArrayList<RegionForecastDate> getForecastDates() {
        return forecastDates;
    }



}