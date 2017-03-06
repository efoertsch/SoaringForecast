package com.fisincorporated.aviationweather.airportweather;


import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

public interface WeatherDisplayPreferences {

    public ObservableBoolean isDisplayRawTafMetar();

    public ObservableBoolean isDecodeTafMetar();

    public ObservableField<String> getAltitudeUnits();

    public ObservableField<String> getWindSpeedUnits();

    public ObservableField<String> getDistanceUnits();

}
