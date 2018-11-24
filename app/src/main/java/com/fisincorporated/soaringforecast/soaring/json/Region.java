package com.fisincorporated.soaringforecast.soaring.json;

import android.databinding.ObservableArrayList;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Region {

    // Format "Friday November 23"
    @SerializedName("printDates")
    @Expose
    private List<String> printDates = null;

    @SerializedName("name")
    @Expose
    private String name;

    // Format "2018-11-23"
    @SerializedName("dates")
    @Expose
    private List<String> dates = null;

    private ArrayList<RegionForecastDate> regionForecastDates = new ObservableArrayList<>();

    public List<String> getPrintDates() {
        return printDates;
    }

    public void setPrintDates(List<String> printDates) {
        this.printDates = printDates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }


    public List<RegionForecastDate> getRegionForecastDateList() {
        return regionForecastDates;
    }

    public void setupRegionForecastDates(){
        regionForecastDates.clear();
        for (int i = 0 ; i < dates.size() ; ++i){
            RegionForecastDate forecastDate = new RegionForecastDate(regionForecastDates.size(), getPrintDate(i), dates.get(i));
            regionForecastDates.add(forecastDate);
        }
    }

    private String getPrintDate(int i) {
        if (printDates != null && i < printDates.size()){
            return printDates.get(i);
        }
        return null;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("printDates")
                .append(Arrays.toString(printDates.toArray()))
                .append(" name").append(name).append(" dates:").append(dates).toString();
    }
}
