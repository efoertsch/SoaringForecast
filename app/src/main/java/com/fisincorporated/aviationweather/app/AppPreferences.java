package com.fisincorporated.aviationweather.app;



import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.fisincorporated.aviationweather.R;

import javax.inject.Inject;
import javax.inject.Named;

import static android.content.Context.MODE_PRIVATE;

public class AppPreferences {

    @Inject
    @Named("app.shared.preferences.name")
    public String AIRPORT_PREFS;

    private static final String AIRPORT_LIST_KEY = "AIRPORT_LIST_KEY";

    private static String RAW_METAR_KEY;

    private static String TEMPERATURE_UNITS_KEY;

    private static String WINDSPEED_UNITS_KEY;

    private static String ALTITUDE_UNITS_KEY;

    private static String DISTANCE_UNITS_KEY;

    private static String DECODE_TAF_METAR_KEY;

    private static String SATELLITE_REGION_KEY;

    private static String DEFAULT_PREFS_SET = "DEFAULT_PREFS_SET";

    private SharedPreferences sharedPreferences;

    private boolean rawTafMetar;

    private boolean decodeTafMetar;

    private String imperialTemperatureUnits;

    private String imperialWindSpeedUnits;

    private String imperialAltitudeUnits;

    private String imperialDistanceUnits;

    private String satelliteRegionUS;


    @Inject
    public AppPreferences(WeatherApplication application) {

        application.getComponent().inject(this);

        Resources res = application.getResources();

        // keys must match to those used in display_perferences.xml
        RAW_METAR_KEY = res.getString(R.string.pref_raw_taf_metar_key);
        TEMPERATURE_UNITS_KEY = res.getString(R.string.pref_units_temp);
        WINDSPEED_UNITS_KEY = res.getString(R.string.pref_units_wind_speed);
        ALTITUDE_UNITS_KEY = res.getString(R.string.pref_units_altitude);
        DISTANCE_UNITS_KEY = res.getString(R.string.pref_units_distance);
        DECODE_TAF_METAR_KEY = res.getString(R.string.pref_decode_taf_metar_key);

        SATELLITE_REGION_KEY = res.getString(R.string.satellite_region);

        // these should never be needed but use these default values
        rawTafMetar = res.getBoolean(R.bool.pref_raw_taf_metar_value);
        decodeTafMetar = res.getBoolean(R.bool.pref_raw_taf_taf_value);
        imperialTemperatureUnits = res.getString(R.string.pref_units_temp_fahrenheit_value);
        imperialWindSpeedUnits = res.getString(R.string.pref_units_speed_knots_value);
        imperialAltitudeUnits = res.getString(R.string.pref_units_altitude_feet_value);
        imperialDistanceUnits = res.getString(R.string.pref_units_distance_statue_miles_label);
        satelliteRegionUS = res.getString(R.string.satellite_region_us);

        // Setting defaults not working (bug in setDefaultValues that doesn't take into account using non default shared preferences)
        // PreferenceManager.setDefaultValues(application, AIRPORT_PREFS,  MODE_PRIVATE, R.xml.display_preferences, false);
        sharedPreferences =  application.getSharedPreferences(AIRPORT_PREFS,  MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(DEFAULT_PREFS_SET, false)){
            setDisplayRawTafMetar(rawTafMetar);
            setDecodeTafMetar(decodeTafMetar);
            setTemperatureDisplay(imperialTemperatureUnits);
            setWindSpeedDisplay(imperialWindSpeedUnits);
            setAltitudeDisplay(imperialAltitudeUnits);
            setDistanceUnitsDisplay(imperialDistanceUnits);
            sharedPreferences.edit().putBoolean(DEFAULT_PREFS_SET, true).commit();
        }

    }

    public String getAirportList() {
        return sharedPreferences.getString(AIRPORT_LIST_KEY, "");
    }

    public void saveAirportList(@NonNull String airportList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AIRPORT_LIST_KEY, airportList);
        editor.apply();
    }

    public boolean isDisplayRawTafMetar() {
        return sharedPreferences.getBoolean(RAW_METAR_KEY, rawTafMetar);
    }

    public void setDisplayRawTafMetar(boolean displayRawMetar) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(RAW_METAR_KEY, displayRawMetar);
        editor.apply();
    }

    public boolean isDecodeTafMetar() {
        return sharedPreferences.getBoolean(DECODE_TAF_METAR_KEY, decodeTafMetar);
    }

    public void setDecodeTafMetar(boolean decodeTafMetar) {
        this.decodeTafMetar = decodeTafMetar;
    }

    public String getTemperatureDisplay() {
        return sharedPreferences.getString(TEMPERATURE_UNITS_KEY, imperialTemperatureUnits);
    }

    public void setTemperatureDisplay(@NonNull String temperatureDisplay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEMPERATURE_UNITS_KEY, temperatureDisplay);
        editor.apply();
    }

    public String getWindSpeedDisplay() {
        return sharedPreferences.getString(WINDSPEED_UNITS_KEY, imperialWindSpeedUnits);
    }

    public void setWindSpeedDisplay(@NonNull String windspeedDisplay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WINDSPEED_UNITS_KEY, windspeedDisplay);
        editor.apply();
    }

    public String getAltitudeDisplay() {
        return sharedPreferences.getString(ALTITUDE_UNITS_KEY, imperialAltitudeUnits);
    }

    public void setAltitudeDisplay(@NonNull String altitudeDisplay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ALTITUDE_UNITS_KEY, altitudeDisplay);
        editor.apply();
    }

    public String getDistanceUnits() {
        return sharedPreferences.getString(DISTANCE_UNITS_KEY, imperialDistanceUnits);
    }

    public void setDistanceUnitsDisplay(@NonNull String distanceUnits) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DISTANCE_UNITS_KEY, distanceUnits);
        editor.apply();
    }

    public String getImperialTemperatureUnits() {
        return imperialTemperatureUnits;
    }

    public String getImperialWindSpeedUnits() {
        return imperialWindSpeedUnits;
    }

    public String getImperialAltitudeUnits() {
        return imperialAltitudeUnits;
    }

    public String getImperialDistanceUnits() {
        return imperialDistanceUnits;
    }

    public static String getAirportListKey() {
        return AIRPORT_LIST_KEY;
    }

    public static String getRawMetarKey() {
        return RAW_METAR_KEY;
    }

    public static String getTemperatureUnitsKey() {
        return TEMPERATURE_UNITS_KEY;
    }

    public static String getWindspeedUnitsKey() {
        return WINDSPEED_UNITS_KEY;
    }

    public static String getAltitudeUnitsKey() {
        return ALTITUDE_UNITS_KEY;
    }

    public static String getDistanceUnitsKey() {
        return DISTANCE_UNITS_KEY;
    }

    public static String getDecodeTafMetarKey() {
        return DECODE_TAF_METAR_KEY;
    }

    public static String getDefaultPrefsSet() {
        return DEFAULT_PREFS_SET;
    }

    public void getSatelliteRegion(){
        sharedPreferences.getString(SATELLITE_REGION_KEY, satelliteRegionUS);
    }
    public void setSatelliteRegion(String region) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SATELLITE_REGION_KEY, region);
        editor.apply();
    }
}
