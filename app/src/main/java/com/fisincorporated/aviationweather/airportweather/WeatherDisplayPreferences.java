package com.fisincorporated.aviationweather.airportweather;


import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

public interface WeatherDisplayPreferences {

    public ObservableBoolean getRawMetarDisplay();

    public ObservableField<String> getAltitudeUnits();

    public ObservableField<String> getWindSpeedUnits();

    public ObservableField<String> getDistanceUnits();

}
