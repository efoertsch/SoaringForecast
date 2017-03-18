package com.fisincorporated.aviationweather.dagger;

import com.fisincorporated.aviationweather.airports.AirportListActivity;
import com.fisincorporated.aviationweather.airportweather.AirportWeatherActivity;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.WeatherApplication;
import com.fisincorporated.aviationweather.drawer.WeatherDrawerActivity;
import com.fisincorporated.aviationweather.settings.SettingsPreferenceFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface DiComponent {

    void inject(WeatherDrawerActivity activity);

    void inject(AirportWeatherActivity activity);

    void inject(AirportListActivity activity);

    void inject(WeatherApplication weatherApplication);

    void inject(SettingsPreferenceFragment settingsPreferenceFragment);

    void inject(AppPreferences appPreferences);



}

