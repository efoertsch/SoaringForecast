package com.fisincorporated.soaringforecast.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// 0 to many Regions held by Regions
public class Region {

    // Format "Friday November 23"
    @SerializedName("printDates")
    @Expose
    private List<String> printDates = null;

    // e.g. "NewEngland", "Panoche",...
    @SerializedName("name")
    @Expose
    private String name;

    // Format "2018-11-23"
    @SerializedName("dates")
    @Expose
    private List<String> dates = null;

    // Custom fields
    private ArrayList<ForecastModels> forecastModelsList = new ArrayList<>();

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

    // Custom methods below
    // Must be added in same order as dates
    // So for each date, you have a list of models (gfs, nam,..) for that date
    public void addForecastModels(ForecastModels forecastModels){
            forecastModelsList.add(forecastModels);
    }

    public List<ForecastModels> getForecastModels() {
        return forecastModelsList;
    }

    public String getDate(int i){
        if (dates != null && i < dates.size()){
            return dates.get(i);
        }
        return null;
    }

    public ForecastModels getForecastModel(int i){
        if (forecastModelsList != null && i < forecastModelsList.size()){
            return forecastModelsList.get(i);
        }
        return null;
    }


    public String getPrintDate(int i) {
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
