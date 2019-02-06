package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.about.AboutActivity;
import org.soaringforecast.rasp.about.AboutFragment;
import org.soaringforecast.rasp.airport.AirportActivity;
import org.soaringforecast.rasp.airport.airportweather.AirportMetarTafFragment;
import org.soaringforecast.rasp.airport.list.AirportListFragment;
import org.soaringforecast.rasp.airport.search.AirportSearchFragment;
import org.soaringforecast.rasp.settings.forecastorder.ForecastOrderFragment;
import org.soaringforecast.rasp.soaring.forecast.ForecastDrawerActivity;
import org.soaringforecast.rasp.satellite.SatelliteActivity;
import org.soaringforecast.rasp.satellite.noaa.NoaaSatelliteImageFragment;
import org.soaringforecast.rasp.settings.SettingsActivity;
import org.soaringforecast.rasp.settings.preferences.SettingsPreferenceFragment;
import org.soaringforecast.rasp.settings.regions.RegionSelectionFragment;
import org.soaringforecast.rasp.soaring.forecast.SoaringForecastFragment;
import org.soaringforecast.rasp.task.TaskActivity;
import org.soaringforecast.rasp.task.edit.EditTaskFragment;
import org.soaringforecast.rasp.task.list.TaskListFragment;
import org.soaringforecast.rasp.task.search.TurnpointSearchFragment;
import org.soaringforecast.rasp.task.turnpoints.download.TurnpointsDownloadFragment;
import org.soaringforecast.rasp.task.turnpoints.seeyou.SeeYouImportFragment;
import org.soaringforecast.rasp.windy.WindyActivity;
import org.soaringforecast.rasp.windy.WindyFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(subcomponents = {})
public abstract class UIBuildersModule {

    @ContributesAndroidInjector(modules = {})
    abstract ForecastDrawerActivity bindWeatherDrawerActivity();

    // ----- Forecasts ------------------------
    @ContributesAndroidInjector(modules = {SoaringForecastModule.class})
    abstract SoaringForecastFragment bindSoaringForecastFragmentSpinner();

    @ContributesAndroidInjector(modules = {SoaringForecastModule.class})
    abstract RegionSelectionFragment bindsRegionSelectionFragment();

    // ----- Windy -------
    @ContributesAndroidInjector(modules = {})
    abstract WindyActivity bindWindyActivity();

    @ContributesAndroidInjector(modules = {SoaringForecastModule.class})
    abstract WindyFragment bindWindyFragment();

    // --- Weather Metar/Taf -------------------
    @ContributesAndroidInjector(modules = {})
    abstract AirportActivity bindAirportActivity();

    @ContributesAndroidInjector(modules = {})
    abstract AirportMetarTafFragment bindAirportMetarTafFragment();

    @ContributesAndroidInjector(modules = {})
    abstract AirportListFragment bindAirportListFragment();

    @ContributesAndroidInjector(modules = {})
    abstract AirportSearchFragment bindAirportSearchFragment();


    // ---- Task/Turnpoints ----
    @ContributesAndroidInjector(modules = {})
    abstract TaskActivity bindTaskActivity();

    @ContributesAndroidInjector(modules = {})
    abstract EditTaskFragment bindEditTaskFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TaskListFragment bindTaskListFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TurnpointSearchFragment bindTurnpointSearchFragment();

    @ContributesAndroidInjector(modules = {})
    abstract SeeYouImportFragment bindSeeYouImportFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TurnpointsDownloadFragment bindTurnpointsImportFragment();


    // ---- Satellite ----
    @ContributesAndroidInjector(modules = {})
    abstract SatelliteActivity bindSatelliteActivity();

    @ContributesAndroidInjector(modules = {})
    abstract NoaaSatelliteImageFragment bindNoaaSatelliteImageFragment();

    // ----- Settings ------------
    @ContributesAndroidInjector(modules = {})
    abstract SettingsActivity bindSettingsActivity();

    @ContributesAndroidInjector(modules = {})
    abstract SettingsPreferenceFragment bindSettingsPreferenceFragment();

    @ContributesAndroidInjector(modules = {})
    abstract ForecastOrderFragment bindForecastOrderFragment();


    // ----- About ------------
    @ContributesAndroidInjector(modules = {})
    abstract AboutActivity bindAboutActivity();

    @ContributesAndroidInjector(modules = {})
    abstract AboutFragment bindAboutFragment();






}


