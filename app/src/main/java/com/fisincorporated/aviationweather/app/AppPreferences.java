package com.fisincorporated.aviationweather.app;



import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.fisincorporated.aviationweather.R;

import javax.inject.Inject;

public class AppPreferences {

//    @Inject
//    @Named("app.shared.preferences.name")
//    public String AIRPORT_PREFS;

    private static final String AIRPORT_LIST_KEY = "AIRPORT_LIST_KEY";

    private static String RAW_METAR_KEY;

    private static String TEMPERATURE_UNITS_KEY;

    private static String WINDSPEED_UNITS_KEY;

    private static String ALTITUDE_UNITS_KEY;

    private static String DISTANCE_UNITS_KEY;

    private SharedPreferences sharedPreferences;

    private boolean rawMetar;

    private String temperatureUnits;

    private String windSpeedUnits;

    private String altitudeUnits;

    private String distanceUnits;


    @Inject
    public AppPreferences(WeatherApplication application) {
        //application.getComponent().inject(this);

        PreferenceManager.setDefaultValues(application, R.xml.display_preferences, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        //sharedPreferences = application.getSharedPreferences(AIRPORT_PREFS, MODE_PRIVATE);
        Resources res = application.getResources();

        // keys must match to those used in display_perferences.xml
        RAW_METAR_KEY = res.getString(R.string.pref_raw_metar_key);
        TEMPERATURE_UNITS_KEY = res.getString(R.string.pref_units_temp);
        WINDSPEED_UNITS_KEY = res.getString(R.string.pref_units_wind_speed);
        ALTITUDE_UNITS_KEY = res.getString(R.string.pref_units_altitude);
        DISTANCE_UNITS_KEY = res.getString(R.string.pref_units_distance);

        // and use these default values
        rawMetar = false;
        temperatureUnits = res.getString(R.string.pref_units_temp_fahrenheit_value);
        windSpeedUnits = res.getString(R.string.pref_units_speed_knots_value);
        altitudeUnits = res.getString(R.string.pref_units_altitude_feet_value);
        distanceUnits = res.getString(R.string.pref_units_distance_statue_miles_label);


    }

    public String getAirportList() {
        return sharedPreferences.getString(AIRPORT_LIST_KEY, "");
    }

    public void saveAirportList(@NonNull String airportList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AIRPORT_LIST_KEY, airportList);
        editor.apply();
    }

    public String getTemperatureDisplay() {
        return sharedPreferences.getString(TEMPERATURE_UNITS_KEY, temperatureUnits);
    }

    public void setTemperatureDisplay(@NonNull String temperatureDisplay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEMPERATURE_UNITS_KEY, temperatureDisplay);
        editor.apply();
    }

    public String getWindSpeedDisplay() {
        return sharedPreferences.getString(WINDSPEED_UNITS_KEY, windSpeedUnits);
    }

    public void setWindSpeedDisplay(@NonNull String windspeedDisplay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WINDSPEED_UNITS_KEY, windspeedDisplay);
        editor.apply();
    }

    public String getAltitudeDisplay() {
        return sharedPreferences.getString(ALTITUDE_UNITS_KEY, altitudeUnits);
    }

    public void setAltitudeDisplay(@NonNull String altitudeDisplay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ALTITUDE_UNITS_KEY, altitudeDisplay);
        editor.apply();
    }

    public boolean getDisplayRawMetar() {
        return sharedPreferences.getBoolean(RAW_METAR_KEY, rawMetar);
    }

    public void setDisplayRawMetar(boolean displayRawMetar) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(RAW_METAR_KEY, displayRawMetar);
        editor.apply();
    }

    public String getDistanceUnits() {
        return sharedPreferences.getString(DISTANCE_UNITS_KEY, distanceUnits);
    }

    public void setDistanceUnitsDisplay(@NonNull String distanceUnits) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DISTANCE_UNITS_KEY, distanceUnits);
        editor.apply();
    }

}
