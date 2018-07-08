package com.fisincorporated.aviationweather.soaring.json;

public class ModelForecastDate extends BaseDate {

    private String model;
    private GpsLocationAndTimes gpsLocationAndTimes;


    public ModelForecastDate(String model) {
        super();
    }

    public ModelForecastDate(String model, int index, String formattedDate, String yyyymmddDate) {
        super(index, formattedDate, yyyymmddDate);
        this.model = model;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setGpsLocationAndTimes(GpsLocationAndTimes gpsLocationAndTimes) {
        this.gpsLocationAndTimes = gpsLocationAndTimes;
    }

    public GpsLocationAndTimes getGpsLocationAndTimes() {
        return gpsLocationAndTimes;
    }
}
