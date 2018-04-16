package com.fisincorporated.aviationweather.soaring.json;


public class RegionForecastDate {

    private int index;
    private String formattedDate;    //"Friday March 30"
    private String yyyymmddDate;     //"2018-03-30"
    private TypeLocationAndTimes typeLocationAndTimes;

    public RegionForecastDate(){};

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
    public String toString(){
        return formattedDate;
    }

    public void copy(RegionForecastDate regionForecastDate) {
        this.index = regionForecastDate.getIndex();
        this.formattedDate = regionForecastDate.getFormattedDate();
        this.yyyymmddDate = regionForecastDate.getYyyymmddDate();
    }

    public void setTypeLocationAndTimes(TypeLocationAndTimes typeLocationAndTimes) {
        this.typeLocationAndTimes = typeLocationAndTimes;
    }

    public TypeLocationAndTimes getTypeLocationAndTimes() {
        return typeLocationAndTimes;
    }
}
