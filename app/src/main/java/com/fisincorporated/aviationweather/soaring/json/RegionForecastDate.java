package com.fisincorporated.aviationweather.soaring.json;


public class RegionForecastDate extends BaseDate {


    private ModelLocationAndTimes modelLocationAndTimes;

    public RegionForecastDate() {
        super();
    }

    public RegionForecastDate(int index, String formattedDate, String yyyymmddDate) {
        super(index, formattedDate, yyyymmddDate);
    }


    public void copy(RegionForecastDate regionForecastDate) {
        index = regionForecastDate.getIndex();
        formattedDate = regionForecastDate.getFormattedDate();
        yyyymmddDate = regionForecastDate.getYyyymmddDate();
    }

    public void setModelLocationAndTimes(ModelLocationAndTimes modelLocationAndTimes) {
        this.modelLocationAndTimes = modelLocationAndTimes;
    }

    public ModelLocationAndTimes getModelLocationAndTimes() {
        return modelLocationAndTimes;
    }
}
