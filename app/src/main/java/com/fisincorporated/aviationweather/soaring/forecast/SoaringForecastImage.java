package com.fisincorporated.aviationweather.soaring.forecast;

import com.fisincorporated.aviationweather.common.BitmapImage;

public class SoaringForecastImage extends BitmapImage {

    private String localTime;

    public SoaringForecastImage(String imageName, String localTime) {
        super(imageName);
        this.localTime = localTime;
    }

    public String getLocalTime() {
        return localTime;
    }

    public void setLocalTime(String localTime) {
        this.localTime = localTime;
    }





}
