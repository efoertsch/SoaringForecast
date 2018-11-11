package com.fisincorporated.soaringforecast.soaring.json;

public class ModelForecastDate extends BaseDate {

    private String model;
    private GpsLocationAndTimes gpsLocationAndTimes;


    public ModelForecastDate(String model) {
        super();
        this.model = model;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelForecastDate) {
            ModelForecastDate c = (ModelForecastDate) obj;
            return c.model.equals(model) && c.yyyymmddDate.equals(yyyymmddDate);
        }

        return false;
    }

    public GpsLocationAndTimes getGpsLocationAndTimes() {
        return gpsLocationAndTimes;
    }
}
