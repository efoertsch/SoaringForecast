package com.fisincorporated.aviationweather.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Forecast {

    @SerializedName("forecast_name")
    @Expose
    private String forecastName;
    @SerializedName("forecast_type")
    @Expose
    private String forecastType;
    @SerializedName("forecast_name_display")
    @Expose
    private String forecastNameDisplay;
    @SerializedName("forecast_description")
    @Expose
    private String forecastDescription;

    public String getForecastName() {
        return forecastName;
    }

    public void setForecastName(String forecastName) {
        this.forecastName = forecastName;
    }

    public String getForecastType() {
        return forecastType;
    }

    public void setForecastType(String forecastType) {
        this.forecastType = forecastType;
    }

    public String getForecastNameDisplay() {
        return forecastNameDisplay;
    }

    public void setForecastNameDisplay(String forecastNameDisplay) {
        this.forecastNameDisplay = forecastNameDisplay;
    }

    public String getForecastDescription() {
        return forecastDescription;
    }

    public void setForecastDescription(String forecastDescription) {
        this.forecastDescription = forecastDescription;
    }

}