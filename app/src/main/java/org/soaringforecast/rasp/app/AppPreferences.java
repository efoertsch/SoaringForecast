package org.soaringforecast.rasp.app;



import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.repository.Airport;
import org.soaringforecast.rasp.satellite.data.SatelliteImageType;
import org.soaringforecast.rasp.satellite.data.SatelliteRegion;
import org.soaringforecast.rasp.soaring.json.Forecasts;
import org.soaringforecast.rasp.soaring.json.ModelForecastDate;
import org.soaringforecast.rasp.utils.JSONResourceReader;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

import static android.content.Context.MODE_PRIVATE;

public class AppPreferences {

    //private static final String AIRPORT_LIST_KEY = "AIRPORT_LIST_KEY";

    private static final String SATELLITE_REGION_KEY = "SATELLITE_REGION";
    private static final String SATELLITE_IMAGE_TYPE_KEY = "SATELLITE_IMAGE_TYPE";
    private static final String DEFAULT_PREFS_SET = "DEFAULT_PREFS_SET";
    private static final String SOARING_FORECAST_MODEL_KEY = "SOARING_FORECAST_MODEL";
    private static final String SOARING_FORECAST_REGION_KEY = "SOARING_FORECAST_REGION";
    private static final String AIRPORT_CODES_FOR_METAR = "AIRPORT_CODES_FOR_METAR_TAF";
    private static final String FORECAST_OVERLAY_OPACITY = "FORECAST_OVERLAY_OPACITY";
    private static final String ICAO_CODE_DELIMITER = " ";
    private static final String SELECTED_TASK_ID = "SELECTED_TASK_ID";
    private static final String WINDY_ZOOM_LEVEL = "WINDY_ZOOM_LEVEL" ;
    private static final String SELECTED_MODEL_FORECAST_DATE = "SELECTED_MODEL_FORECAST_DATE";
    private static final String ORDERED_FORECAST_OPTIONS = "ORDERED_FORECAST_OPTIONS";

    // These string values are assigned in code so they match what is used in Settings
    private static String DISPLAY_WINDY_MENU_OPTION;
    private static String DISPLAY_SKYSIGHT_MENU_OPTION;
    private static String DISPLAY_DR_JACKS_MENU_OPTION;
    private static String RAW_METAR_KEY;
    private static String TEMPERATURE_UNITS_KEY;
    private static String WINDSPEED_UNITS_KEY;
    private static String ALTITUDE_UNITS_KEY;
    private static String DISTANCE_UNITS_KEY;
    private static String DECODE_TAF_METAR_KEY;
    private static String SUA_DISPLAY_KEY;
    private static String DISPLAY_FORECAST_SOUNDINGS;

    private SharedPreferences sharedPreferences;
    private boolean rawTafMetar;
    private boolean decodeTafMetar;
    private String imperialTemperatureUnits;
    private String imperialWindSpeedUnits;
    private String imperialAltitudeUnits;
    private String imperialDistanceUnits;
    private String satelliteRegionUS;
    private String satelliteImageTypeVis;
    private String soaringForecastModel;
    private String soaringForecastDefaultRegion;

    @Inject
    public AppPreferences(Context context, String airportPrefs) {

        String airportPrefs1 = airportPrefs;

        Resources res = context.getResources();

        // keys must match to those used in display_perferences.xml
        RAW_METAR_KEY = res.getString(R.string.pref_raw_taf_metar_key);
        TEMPERATURE_UNITS_KEY = res.getString(R.string.pref_units_temp);
        WINDSPEED_UNITS_KEY = res.getString(R.string.pref_units_wind_speed);
        ALTITUDE_UNITS_KEY = res.getString(R.string.pref_units_altitude);
        DISTANCE_UNITS_KEY = res.getString(R.string.pref_units_distance);
        DECODE_TAF_METAR_KEY = res.getString(R.string.pref_decode_taf_metar_key);
        SUA_DISPLAY_KEY = res.getString(R.string.pref_display_forecast_sua_key);

        soaringForecastDefaultRegion = context.getString(R.string.new_england_region);


        DISPLAY_WINDY_MENU_OPTION =  context.getString(R.string.pref_add_windy_to_menu_key);
        DISPLAY_SKYSIGHT_MENU_OPTION = context.getString(R.string.pref_add_skysight_to_menu_key);
        DISPLAY_DR_JACKS_MENU_OPTION = context.getString(R.string.pref_add_dr_jacks_to_menu_key);

        DISPLAY_FORECAST_SOUNDINGS = context.getString(R.string.pref_display_forecast_soundings_key);

        // these should never be needed but use these default values
        rawTafMetar = res.getBoolean(R.bool.pref_raw_taf_metar_value);
        decodeTafMetar = res.getBoolean(R.bool.pref_raw_taf_taf_value);
        imperialTemperatureUnits = res.getString(R.string.pref_units_temp_fahrenheit_value);
        imperialWindSpeedUnits = res.getString(R.string.pref_units_speed_knots_value);
        imperialAltitudeUnits = res.getString(R.string.pref_units_altitude_feet_value);
        imperialDistanceUnits = res.getString(R.string.pref_units_distance_statue_miles_label);
        satelliteRegionUS = res.getString(R.string.satellite_region_us);
        satelliteImageTypeVis = res.getString(R.string.satellite_image_type_vis);
        soaringForecastModel = res.getString(R.string.default_forecast_model);

        // Setting defaults not working (bug in setDefaultValues that doesn't take into account using non default shared preferences)
        // PreferenceManager.setDefaultValues(application, AIRPORT_PREFS,  MODE_PRIVATE, R.xml.display_preferences, false);
        sharedPreferences = context.getSharedPreferences(airportPrefs1, MODE_PRIVATE);
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

    public String getForecastModel() {
        return sharedPreferences.getString(SOARING_FORECAST_MODEL_KEY, soaringForecastModel);
    }

    public void setForecastModel(String selectedModel) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SOARING_FORECAST_MODEL_KEY, selectedModel);
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


    public boolean isAnyForecastOptionDisplayed(){
        return isWindyDisplayed() || isSkySightDisplayed() || isDrJacksDisplayed();
    }

    public boolean isWindyDisplayed(){
        return sharedPreferences.getBoolean(DISPLAY_WINDY_MENU_OPTION, true);
    }

    public boolean isSkySightDisplayed(){
        return sharedPreferences.getBoolean(DISPLAY_SKYSIGHT_MENU_OPTION, false);
    }

    public boolean isDrJacksDisplayed(){
        return sharedPreferences.getBoolean(DISPLAY_DR_JACKS_MENU_OPTION, false);
    }

    public void setForecastOverlayOpacity(int opacity){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(FORECAST_OVERLAY_OPACITY, opacity);
        editor.apply();
    }

    public int getForecastOverlayOpacity(){
       return sharedPreferences.getInt(FORECAST_OVERLAY_OPACITY, 30);
    }

    public void setDisplayForecastSoundings(boolean display){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DISPLAY_FORECAST_SOUNDINGS,  display);
        editor.apply();
    }

    public boolean getDisplayForecastSoundings(){
        return sharedPreferences.getBoolean(DISPLAY_FORECAST_SOUNDINGS, true);
    }

    public long getSelectedTaskId(){
        return sharedPreferences.getLong(SELECTED_TASK_ID, -1L);
    }

    public void setSelectedTaskId(long selectedTaskId){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(SELECTED_TASK_ID, selectedTaskId);
        editor.apply();
    }

    public double getWindyZoomLevel() {
        return sharedPreferences.getInt(WINDY_ZOOM_LEVEL, 7);
    }

    public void setSelectedModelForecastDate(ModelForecastDate selectedModelForecastDate) {
        Gson gson = new Gson();
        String json = gson.toJson(selectedModelForecastDate);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SELECTED_MODEL_FORECAST_DATE, json);
        editor.apply();
    }

    public ModelForecastDate getSelectedModelForecastDate() {
        Gson gson = new Gson();
        return gson.fromJson(sharedPreferences.getString(SELECTED_MODEL_FORECAST_DATE, "")
                ,ModelForecastDate.class);
    }

    // --------- Forecasts -----------------
    public Observable<Forecasts> getOrderedForecastList() {
        return Observable.create(emitter -> {
            try {
                Forecasts forecasts = JSONResourceReader.constructUsingGson(
                        sharedPreferences.getString(ORDERED_FORECAST_OPTIONS,""),Forecasts.class);
                if (forecasts != null && forecasts.getForecasts().size() > 0) {
                    emitter.onNext(forecasts);
                } else {
                    emitter.onNext(new Forecasts());
                }
                emitter.onComplete();
            } catch (JsonSyntaxException jse) {
                emitter.onError(jse);
            }
        });
    }

    public void setOrderedForecastList(Forecasts forecasts){
        Gson gson = new Gson();
        String json = gson.toJson(forecasts);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ORDERED_FORECAST_OPTIONS, json);
        editor.apply();
    }

    public void deleteCustomForecastOrder() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(ORDERED_FORECAST_OPTIONS);
        editor.apply();
    }

    public void setDisplaySua(boolean displaySua){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SUA_DISPLAY_KEY, displaySua);
        editor.apply();
    }

    public boolean getDisplaySua(){
        return sharedPreferences.getBoolean(SUA_DISPLAY_KEY, true);
    }
}
