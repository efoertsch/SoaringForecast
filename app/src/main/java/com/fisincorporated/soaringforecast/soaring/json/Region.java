package com.fisincorporated.soaringforecast.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class Region {

    @SerializedName("printDates")
    @Expose
    private List<String> printDates = null;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("dates")
    @Expose
    private List<String> dates = null;

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

    @Override
    public String toString() {
        return new StringBuilder().append("printDates")
                .append(Arrays.toString(printDates.toArray()))
                .append(" name").append(name).append(" dates:").append(dates).toString();
    }
}
