package com.fisincorporated.aviationweather.app;



import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.repository.Airport;
import com.fisincorporated.aviationweather.satellite.data.SatelliteImageType;
import com.fisincorporated.aviationweather.satellite.data.SatelliteRegion;
import com.fisincorporated.aviationweather.soaring.forecast.SoaringForecastModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;

public class AppPreferences {

    //private static final String AIRPORT_LIST_KEY = "AIRPORT_LIST_KEY";

    private static final String SATELLITE_REGION_KEY = "SATELLITE_REGION";

    private static final String SATELLITE_IMAGE_TYPE_KEY = "SATELLITE_IMAGE_TYPE";

    private static final String DEFAULT_PREFS_SET = "DEFAULT_PREFS_SET";

    private static final String SOARING_FORECAST_TYPE_KEY = "SOARING_FORECAST_TYPE";

    private static final String SOARING_FORECAST_REGION_KEY = "SOARING_FORECAST_REGION";

    private static final String AIRPORT_CODES_FOR_METAR = "AIRPORT_CODES_FOR_METAR_TAF";

    private static final String ICAO_CODE_DELIMITER = " ";

    private static String DISPLAY_SKYSIGHT_MENU_OPTION;

    private static String DISPLAY_DR_JACKS_MENU_OPTION;

    private static String RAW_METAR_KEY;

    private static String TEMPERATURE_UNITS_KEY;

    private static String WINDSPEED_UNITS_KEY;

    private static String ALTITUDE_UNITS_KEY;

    private static String DISTANCE_UNITS_KEY;

    private static String DECODE_TAF_METAR_KEY;

    private SharedPreferences sharedPreferences;

    private boolean rawTafMetar;

    private boolean decodeTafMetar;

    private String imperialTemperatureUnits;

    private String imperialWindSpeedUnits;

    private String imperialAltitudeUnits;

    private String imperialDistanceUnits;

    private String satelliteRegionUS;

    private String satelliteImageTypeVis;

    private String soaringForecastType;

    private String soaringForecastDefaultRegion;

    private String airportPrefs;


    @Inject
    public AppPreferences(Context context, String airportPrefs) {

        this.airportPrefs = airportPrefs;

        Resources res = context.getResources();

        // keys must match to those used in display_perferences.xml
        RAW_METAR_KEY = res.getString(R.string.pref_raw_taf_metar_key);
        TEMPERATURE_UNITS_KEY = res.getString(R.string.pref_units_temp);
        WINDSPEED_UNITS_KEY = res.getString(R.string.pref_units_wind_speed);
        ALTITUDE_UNITS_KEY = res.getString(R.string.pref_units_altitude);
        DISTANCE_UNITS_KEY = res.getString(R.string.pref_units_distance);
        DECODE_TAF_METAR_KEY = res.getString(R.string.pref_decode_taf_metar_key);
        soaringForecastDefaultRegion = context.getString(R.string.new_england_region);

        DISPLAY_SKYSIGHT_MENU_OPTION = context.getString(R.string.pref_add_skysight_to_menu_key);
        DISPLAY_DR_JACKS_MENU_OPTION = context.getString(R.string.pref_add_dr_jacks_to_menu_key);

        // these should never be needed but use these default values
        rawTafMetar = res.getBoolean(R.bool.pref_raw_taf_metar_value);
        decodeTafMetar = res.getBoolean(R.bool.pref_raw_taf_taf_value);
        imperialTemperatureUnits = res.getString(R.string.pref_units_temp_fahrenheit_value);
        imperialWindSpeedUnits = res.getString(R.string.pref_units_speed_knots_value);
        imperialAltitudeUnits = res.getString(R.string.pref_units_altitude_feet_value);
        imperialDistanceUnits = res.getString(R.string.pref_units_distance_statue_miles_label);
        satelliteRegionUS = res.getString(R.string.satellite_region_us);
        satelliteImageTypeVis = res.getString(R.string.satellite_image_type_vis);
        soaringForecastType = res.getString(R.string.soaring_forecast_gfs);

        // Setting defaults not working (bug in setDefaultValues that doesn't take into account using non default shared preferences)
        // PreferenceManager.setDefaultValues(application, AIRPORT_PREFS,  MODE_PRIVATE, R.xml.display_preferences, false);
        sharedPreferences = context.getSharedPreferences(this.airportPrefs, MODE_PRIVATE);
        if (!sharedPreferences.getBoolean(DEFAULT_PREFS_SET, false)) {
            setDisplayRawTafMetar(rawTafMetar);
            setDecodeTafMetar(decodeTafMetar);
            setTemperatureDisplay(imperialTemperatureUnits);
            setWindSpeedDisplay(imperialWindSpeedUnits);
            setAltitudeDisplay(imperialAltitudeUnits);
            setDistanceUnitsDisplay(imperialDistanceUnits);
            sharedPreferences.edit().putBoolean(DEFAULT_PREFS_SET, true).apply();
        }

    }

    public String getAirportList() {
        return sharedPreferences.getString(AIRPORT_CODES_FOR_METAR, "");
    }

    public void saveAirportList(@NonNull String airportList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AIRPORT_CODES_FOR_METAR, airportList);
        editor.apply();
    }

    public boolean isDisplayRawTafMetar() {
        return sharedPreferences.getBoolean(RAW_METAR_KEY, rawTafMetar);
    }

    private void setDisplayRawTafMetar(boolean displayRawMetar) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(RAW_METAR_KEY, displayRawMetar);
        editor.apply();
    }

    public boolean isDecodeTafMetar() {
        return sharedPreferences.getBoolean(DECODE_TAF_METAR_KEY, decodeTafMetar);
    }

    private void setDecodeTafMetar(boolean decodeTafMetar) {
        this.decodeTafMetar = decodeTafMetar;
    }

    public String getTemperatureDisplay() {
        return sharedPreferences.getString(TEMPERATURE_UNITS_KEY, imperialTemperatureUnits);
    }

    private void setTemperatureDisplay(@NonNull String temperatureDisplay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEMPERATURE_UNITS_KEY, temperatureDisplay);
        editor.apply();
    }

    public String getWindSpeedDisplay() {
        return sharedPreferences.getString(WINDSPEED_UNITS_KEY, imperialWindSpeedUnits);
    }

    private void setWindSpeedDisplay(@NonNull String windspeedDisplay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(WINDSPEED_UNITS_KEY, windspeedDisplay);
        editor.apply();
    }

    public String getAltitudeDisplay() {
        return sharedPreferences.getString(ALTITUDE_UNITS_KEY, imperialAltitudeUnits);
    }

    private void setAltitudeDisplay(@NonNull String altitudeDisplay) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ALTITUDE_UNITS_KEY, altitudeDisplay);
        editor.apply();
    }

    public String getDistanceUnits() {
        return sharedPreferences.getString(DISTANCE_UNITS_KEY, imperialDistanceUnits);
    }

    private void setDistanceUnitsDisplay(@NonNull String distanceUnits) {
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
        return AIRPORT_CODES_FOR_METAR;
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

    public SatelliteRegion getSatelliteRegion() {
        return new SatelliteRegion(sharedPreferences.getString(SATELLITE_REGION_KEY, satelliteRegionUS));
    }

    public void setSatelliteRegion(SatelliteRegion satelliteRegion) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SATELLITE_REGION_KEY, satelliteRegion.toStore());
        editor.apply();
    }

    public SatelliteImageType getSatelliteImageType() {
        return new SatelliteImageType(sharedPreferences.getString(SATELLITE_IMAGE_TYPE_KEY, satelliteImageTypeVis));
    }

    public void setSatelliteImageType(SatelliteImageType satelliteImageType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SATELLITE_IMAGE_TYPE_KEY, satelliteImageType.toStore());
        editor.apply();
    }

    public SoaringForecastModel getSoaringForecastType() {
        return new SoaringForecastModel(sharedPreferences.getString(SOARING_FORECAST_TYPE_KEY, soaringForecastType));
    }

    public void setSoaringForecastType(SoaringForecastModel soaringForecastModel) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SOARING_FORECAST_TYPE_KEY, soaringForecastModel.toStore());
        editor.apply();
    }

    public String getSoaringForecastRegion() {
        return sharedPreferences.getString(SOARING_FORECAST_REGION_KEY, soaringForecastDefaultRegion);
    }

    public void setSoaringForecastRegion(String region) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SOARING_FORECAST_REGION_KEY, region);
        editor.apply();
    }

    /**
     * @param icaoCodes space delimited list of icao codes eg "KORH KBOS ..."
     */
    public void setSelectedAirportCodes(@NonNull String icaoCodes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AIRPORT_CODES_FOR_METAR, icaoCodes);
        editor.apply();

    }

    /**
     * @return space delimited list of icao codes eg "KORH KBOS ..."
     */
    public String getSelectedAirportCodes() {
        return sharedPreferences.getString(AIRPORT_CODES_FOR_METAR, "");
    }

    /**
     * @param icaoCodes list of icao codes eg KORH, KBOS, ...
     */
    public void setSelectedAirportCodes(@NonNull List<String> icaoCodes) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AIRPORT_CODES_FOR_METAR, TextUtils.join(ICAO_CODE_DELIMITER, icaoCodes));
        editor.apply();
    }

    /**
     * @return List of icao airport codes eg KORH, KBOS, ...
     */
    public List<String> getSelectedAirportCodesList() {
        String airportCodes = sharedPreferences.getString(AIRPORT_CODES_FOR_METAR, "");
        return new ArrayList<>(Arrays.asList(airportCodes.trim().split("\\s+")));
    }

    public void addAirportCodeToSelectedIcaoCodes(String icaoCode) {
        String oldIcaoCodes = getSelectedAirportCodes();
        if (!oldIcaoCodes.contains(icaoCode)) {
            String newSelectedIcaoCodes = getSelectedAirportCodes() + ICAO_CODE_DELIMITER + icaoCode;
            setSelectedAirportCodes(newSelectedIcaoCodes);
        }
    }

    public void storeNewAirportOrder(List<Airport> airports) {
        StringBuffer sb = new StringBuffer();
        if (airports != null) {
            for (Airport airport : airports) {
                sb.append(airport.getIdent());
                sb.append(ICAO_CODE_DELIMITER);
            }
        }
        setSelectedAirportCodes(sb.toString());
    }

    public boolean isSkySightDisplayed(){
        return sharedPreferences.getBoolean(DISPLAY_SKYSIGHT_MENU_OPTION, false);
    }

    public boolean isDrJacksDisplayed(){
        return sharedPreferences.getBoolean(DISPLAY_DR_JACKS_MENU_OPTION, false);
    }

}
