package com.fisincorporated.aviationweather.dagger;

import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.WeatherApplication;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    public static final String AIRPORT_PREFS = "AIRPORT_PREFS";

    @Provides
    @Named("app.shared.preferences.name")
    public String providesAppSharedPreferencesName() {
        return AIRPORT_PREFS;
    }

    private WeatherApplication application;

    public AppModule(WeatherApplication application) {

        this.application = application;
        // only doing this to set default preferences if needed
        provideAppPreferences();
    }

    @Provides
    @Singleton
    public AppPreferences provideAppPreferences(){
        return new AppPreferences(application);

    }
}
