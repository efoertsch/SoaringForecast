package com.fisincorporated.aviationweather.airportweather;


public interface WeatherDisplayPreferences {

    boolean isDisplayRawTafMetar();

    boolean isDecodeTafMetar();

    String getAltitudeUnits();

    String getWindSpeedUnits();

    String getDistanceUnits();

}
