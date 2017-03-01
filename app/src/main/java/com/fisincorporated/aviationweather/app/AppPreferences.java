package com.fisincorporated.aviationweather.app;



import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Named;

import static android.content.Context.MODE_PRIVATE;

public class AppPreferences {

    @Inject @Named("app.shared.preferences.name")
    public String AIRPORT_PREFS ;

    @Inject @Named("airport.code.list.pref_key")
    public String AIRPORT_LIST;

    @Inject
    public AppPreferences() {
    }

    public String getAirportList(@NonNull Context context){
        // Restore preferences
        SharedPreferences preferences = context.getSharedPreferences(AIRPORT_PREFS,  MODE_PRIVATE);
        return preferences.getString(AIRPORT_LIST, "");

    }

    public void saveAirportList(@NonNull Context context, @NonNull String airportList) {
        SharedPreferences preferences = context.getSharedPreferences(AIRPORT_PREFS,  MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(AIRPORT_LIST, airportList);
        editor.apply();
    }
}
