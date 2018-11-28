package com.fisincorporated.soaringforecast.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * List of forecasts (wstar, hcrit, ...)
 */
public class Forecasts {

    @SerializedName("forecasts")
    @Expose
    private List<Forecast> forecasts = null;

    public List<Forecast> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<Forecast> forecasts) {
        this.forecasts = forecasts;
    }


}