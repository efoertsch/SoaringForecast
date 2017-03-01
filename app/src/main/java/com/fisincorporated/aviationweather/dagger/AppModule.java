package com.fisincorporated.aviationweather.dagger;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    public static final String AIRPORT_PREFS = "AIRPORT_PREFS";

    public static final String AIRPORT_LIST_KEY = "AIRPORT_LIST_KEY";

    @Provides
    @Named("app.shared.preferences.name")
    public String providesAppSharedPreferencesName() {
       return AIRPORT_PREFS;
    }

    @Provides @Named("airport.code.list.pref_key")
    public String providesAirportCodeListPrefKey() {
       return AIRPORT_LIST_KEY;
    }


}
