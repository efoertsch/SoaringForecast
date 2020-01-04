package org.soaringforecast.rasp.dagger;

import org.soaringforecast.rasp.about.AboutActivity;
import org.soaringforecast.rasp.about.AboutFragment;
import org.soaringforecast.rasp.airport.AirportActivity;
import org.soaringforecast.rasp.airport.airportweather.AirportMetarTafFragment;
import org.soaringforecast.rasp.airport.list.AirportListFragment;
import org.soaringforecast.rasp.airport.search.AirportSearchFragment;
import org.soaringforecast.rasp.satellite.SatelliteActivity;
import org.soaringforecast.rasp.satellite.noaa.NoaaSatelliteImageFragment;
import org.soaringforecast.rasp.settings.SettingsActivity;
import org.soaringforecast.rasp.settings.forecastorder.ForecastOrderFragment;
import org.soaringforecast.rasp.settings.preferences.SettingsPreferenceFragment;
import org.soaringforecast.rasp.settings.regions.RegionSelectionFragment;
import org.soaringforecast.rasp.soaring.forecast.ForecastDrawerActivity;
import org.soaringforecast.rasp.soaring.forecast.SoaringForecastFragment;
import org.soaringforecast.rasp.task.TaskActivity;
import org.soaringforecast.rasp.task.edit.EditTaskFragment;
import org.soaringforecast.rasp.task.list.TaskListFragment;
import org.soaringforecast.rasp.turnpoints.TurnpointActivity;
import org.soaringforecast.rasp.turnpoints.download.TurnpointsDownloadFragment;
import org.soaringforecast.rasp.turnpoints.edit.TurnpointEditFragment;
import org.soaringforecast.rasp.turnpoints.list.TurnpointListFragment;
import org.soaringforecast.rasp.turnpoints.search.TurnpointSearchForEditFragment;
import org.soaringforecast.rasp.turnpoints.search.TurnpointSearchForTaskFragment;
import org.soaringforecast.rasp.turnpoints.seeyou.SeeYouImportFragment;
import org.soaringforecast.rasp.turnpoints.turnpointview.TurnpointSatelliteViewFragment;
import org.soaringforecast.rasp.windy.WindyActivity;
import org.soaringforecast.rasp.windy.WindyFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(subcomponents = {})
public abstract class UIBuildersModule {

    @ContributesAndroidInjector(modules = {})
    abstract ForecastDrawerActivity bindWeatherDrawerActivity();

    // ----- Forecasts ------------------------
    @ContributesAndroidInjector(modules = {})
    abstract SoaringForecastFragment bindSoaringForecastFragment();

    @ContributesAndroidInjector(modules = {})
    abstract RegionSelectionFragment bindsRegionSelectionFragment();

    // ----- Windy -------
    @ContributesAndroidInjector(modules = {})
    abstract WindyActivity bindWindyActivity();

    @ContributesAndroidInjector(modules = {WindyModule.class})
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


    // ---- Task ----
    @ContributesAndroidInjector(modules = {})
    abstract TaskActivity bindTaskActivity();

    @ContributesAndroidInjector(modules = {})
    abstract EditTaskFragment bindEditTaskFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TaskListFragment bindTaskListFragment();

    // ---- Turnpoints ----
    @ContributesAndroidInjector(modules = {})
    abstract TurnpointActivity bindTurnpointActivity();

    @ContributesAndroidInjector(modules = {})
    abstract TurnpointListFragment bindTurnpointListFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TurnpointSearchForTaskFragment bindTurnpointSearchForTaskFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TurnpointSearchForEditFragment bindTurnpointSearchForEditFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TurnpointEditFragment bindTurnpointEditFragment();

    @ContributesAndroidInjector(modules = {})
    abstract SeeYouImportFragment bindSeeYouImportFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TurnpointsDownloadFragment bindTurnpointsImportFragment();

    @ContributesAndroidInjector(modules = {})
    abstract TurnpointSatelliteViewFragment bindTurnpointSatelliteViewFragment();


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


