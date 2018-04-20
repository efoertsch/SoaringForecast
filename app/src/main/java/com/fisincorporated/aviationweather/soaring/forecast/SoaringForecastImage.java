package com.fisincorporated.aviationweather.soaring.forecast;

import com.fisincorporated.aviationweather.common.BitmapImage;

public class SoaringForecastImage extends BitmapImage {

    private String forecastTime = null;  //1300
    private String forecastType = null;  // nam
    private String bitmapType = null;  // body footer header
    private String region = null;     // NewEngland
    private String forecastParameter = null;   // wstar
    private String yyyymmdd = null;


    public SoaringForecastImage(String imageName) {
        super(imageName);
    }

    // For convenience save all the individual bits
    public String getForecastTime() {
        return forecastTime;
    }

    public SoaringForecastImage setForecastTime(String forecastTime) {
        this.forecastTime = forecastTime;
        return this;
    }

    public String getForecastType() {
        return forecastType;
    }

    public SoaringForecastImage setForecastType(String forecastType) {
        this.forecastType = forecastType;
        return this;
    }

    public String getBitmapType() {
        return bitmapType;
    }

    public SoaringForecastImage setBitmapType(String bitmapType) {
        this.bitmapType = bitmapType;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public SoaringForecastImage setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getForecastParameter() {
        return forecastParameter;
    }

    public SoaringForecastImage setForecastParameter(String forecastParameter) {
        this.forecastParameter = forecastParameter;
        return this;
    }

    public String getYyyymmdd() {
        return yyyymmdd;
    }

    public SoaringForecastImage setYyyymmdd(String yyyymmdd) {
        this.yyyymmdd = yyyymmdd;
        return this;
    }
}
