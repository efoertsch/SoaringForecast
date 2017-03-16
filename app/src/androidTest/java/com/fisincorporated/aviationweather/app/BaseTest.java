package com.fisincorporated.aviationweather.app;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;

import com.fisincorporated.aviationweather.dagger.AppModule;

import utils.RecyclerViewMatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class BaseTest {

    public static final String airportList = "KORH KFIT";

    public  String spName = AppModule.AIRPORT_PREFS;

    public Context targetContext;


    public void setup() {
        targetContext = InstrumentationRegistry.getTargetContext();
    }

    public Context getContext() {
        return targetContext ;
    }

    public SharedPreferences getSharedPreferences() {
        return targetContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    public void setSharedPreferenceAirportList(String airports) {
        SharedPreferences sp = getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(AppPreferences.getAirportListKey(), airports);
        editor.commit();
    }

    // See https://spin.atomicobject.com/2016/04/15/espresso-testing-recyclerviews/
    // Convenience helper
    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    public static void scrollToRecyclerPosition(int recyclerViewId, int position) {
        onView(withId(recyclerViewId)).perform(scrollToPosition(position));

    }
}


