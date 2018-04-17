package com.fisincorporated.aviationweather.soaring.json;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegionForecastDate {

    private static DateFormat yyyymmddFormat = new SimpleDateFormat("yyyy-MM-dd" );
    private static final DateFormat formattedDateFormat = new SimpleDateFormat(("EEE, MMM dd"));

    private int index;
    private String formattedDate;    //"Friday March 30"
    private String yyyymmddDate;     //"2018-03-30"
    private TypeLocationAndTimes typeLocationAndTimes;

    public RegionForecastDate() {
        index = 0;
        Date date = new Date();
        formattedDate = formattedDateFormat.format(date);
        yyyymmddDate = yyyymmddFormat.format(date);
    }

    public RegionForecastDate(int index, String formattedDate, String yyyymmddDate) {
        this.index = index;
        this.formattedDate = formattedDate;
        this.yyyymmddDate = yyyymmddDate;
    }

    public int getIndex() {
        return index;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public String getYyyymmddDate() {
        return yyyymmddDate;
    }

    public void setYyyymmddDate(String yyyymmddDate) {
        this.yyyymmddDate = yyyymmddDate;
    }

    // For date display
    @Override
    public String toString() {
        return formattedDate;
    }

    public void copy(RegionForecastDate regionForecastDate) {
        index = regionForecastDate.getIndex();
        formattedDate = regionForecastDate.getFormattedDate();
        yyyymmddDate = regionForecastDate.getYyyymmddDate();
    }

    public void setTypeLocationAndTimes(TypeLocationAndTimes typeLocationAndTimes) {
        this.typeLocationAndTimes = typeLocationAndTimes;
    }

    public TypeLocationAndTimes getTypeLocationAndTimes() {
        return typeLocationAndTimes;
    }
}
