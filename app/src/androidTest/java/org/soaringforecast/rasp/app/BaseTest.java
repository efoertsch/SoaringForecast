package org.soaringforecast.rasp.app;


import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.platform.app.InstrumentationRegistry;

import utils.RecyclerViewMatcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Used to set various values/classes used in testing.
 */
public class BaseTest {

    protected static final String airportList = "KORH KFIT";

    //Making it simple(?) and getting shared prefences name this way rather than using injection
    private  static final String AIRPORT_PREFS_TEST_NAME = SoaringWeatherApplicationTest.AIRPORT_PREFS_TEST;

    private Context targetContext;

    public void setup() {
        targetContext = InstrumentationRegistry.getTargetContext();
    }

    protected Context getContext() {
        return targetContext ;
    }

    private SharedPreferences getSharedPreferences() {
        return targetContext.getSharedPreferences(AIRPORT_PREFS_TEST_NAME, Context.MODE_PRIVATE);
    }

    protected void setSharedPreferenceAirportList(String airports) {
        SharedPreferences sp = getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(AppPreferences.getAirportListKey(), airports);
        editor.commit();
    }

    // See https://spin.atomicobject.com/2016/04/15/espresso-testing-recyclerviews/
    // Convenience helper
    protected static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    protected static void scrollToRecyclerPosition(int recyclerViewId, int position) {
        onView(withId(recyclerViewId)).perform(scrollToPosition(position));

    }
}


