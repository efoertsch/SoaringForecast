package com.fisincorporated.aviationweather.soaring.forecast;

import java.util.ArrayList;
import java.util.List;

public class SoaringForecastDate {

    private String formattedDate;
    private String yyyymmddDate;
    private List<SoaringForecastImage> soaringForecastImages = new ArrayList<>();

    public SoaringForecastDate(String formattedDate, String yyyymmddDate) {
        this.formattedDate = formattedDate;
        this.yyyymmddDate = yyyymmddDate;
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

    public void addSoaringForecastImage(SoaringForecastImage soaringForecastImage, int index) {
        soaringForecastImages.add(index, soaringForecastImage);
    }

    public List<SoaringForecastImage> getSoaringForecastImages() {
        return soaringForecastImages;
    }

    public SoaringForecastImage getSoaringForecastImage(int index){
        if (index < soaringForecastImages.size()) {
            return soaringForecastImages.get(index);
        }
        return null;
    }
}
