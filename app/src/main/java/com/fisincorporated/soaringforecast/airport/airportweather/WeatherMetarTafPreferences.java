package com.fisincorporated.soaringforecast.airport.airportweather;


public interface WeatherMetarTafPreferences {

    boolean isDisplayRawTafMetar();

    boolean isDecodeTafMetar();

    String getAltitudeUnits();

    String getWindSpeedUnits();

    String getDistanceUnits();

}
