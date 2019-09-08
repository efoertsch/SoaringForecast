package org.soaringforecast.rasp.soaring;

import android.content.Context;

import org.soaringforecast.rasp.app.AppPreferences;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppPreferencesModule {
    private static final String AIRPORT_PREFS = "AIRPORT_PREFS";

    @Provides
    @Named(AIRPORT_PREFS)
    public String providesAppSharedPreferencesName() {
        return AIRPORT_PREFS;
    }

    @Provides
    @Singleton
    public AppPreferences provideAppPreferences(Context context) {
        return new AppPreferences(context, AIRPORT_PREFS);
    }
}
