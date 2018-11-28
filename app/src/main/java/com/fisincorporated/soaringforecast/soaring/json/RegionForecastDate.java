package com.fisincorporated.soaringforecast.soaring.json;


public class RegionForecastDate extends BaseDate {

    private ForecastModels forecastModels;

    public RegionForecastDate(int index, String formattedDate, String yyyymmddDate) {
        super(index, formattedDate, yyyymmddDate);
    }

    public void setForecastModels(ForecastModels forecastModels) {
        this.forecastModels = forecastModels;
    }

    public ForecastModels getForecastModels() {
        return forecastModels;
    }
}
