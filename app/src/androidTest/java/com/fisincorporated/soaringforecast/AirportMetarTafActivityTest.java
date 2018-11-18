package com.fisincorporated.soaringforecast;


import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.fisincorporated.soaringforecast.app.BaseTest;
import com.fisincorporated.soaringforecast.drawer.WeatherDrawerActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import timber.log.Timber;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AirportMetarTafActivityTest extends BaseTest {

    @Rule
    public ActivityTestRule<WeatherDrawerActivity> mActivityRule =
            new ActivityTestRule<>(WeatherDrawerActivity.class, true, false);

    @Before
    public void setup() {
        Timber.d("Setup called");
        super.setup();
        setSharedPreferenceAirportList(airportList);
    }

    @Test
    public void airportInfoHasMetarFields() {
        Context targetContext = getContext();
        Intent intent = new Intent(targetContext, WeatherDrawerActivity.class);

        mActivityRule.launchActivity(intent);

        String[] airports = airportList.split("\\s+");
        for (int i = 0; i < airports.length; ++i) {

            scrollToRecyclerPosition(R.id.airport_metar_taf_recycler_view, i);
            ViewInteraction viewInteraction = onView(withRecyclerView(R.id.airport_metar_taf_recycler_view).atPosition(i));
            // Check that metar layout exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_weather_include_metar))));
            // Check that metar field exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_station_raw_text))));
        }
    }

    @Test
    public void airportInfoHasTafFields() {
        Context targetContext = getContext();
        Intent intent = new Intent(targetContext, WeatherDrawerActivity.class);

        mActivityRule.launchActivity(intent);

        String[] airports = airportList.split("\\s+");
        for (int i = 0; i < airports.length; ++i) {

            scrollToRecyclerPosition(R.id.airport_metar_taf_recycler_view, i);
            ViewInteraction viewInteraction = onView(withRecyclerView(R.id.airport_metar_taf_recycler_view).atPosition(i));
            // Check that metar layout exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_weather_include_taf))));
            // Check that metar field exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_taf_raw_forecast))));
        }
    }

    @Test
    public void airportsAreDisplayedInOrder() {
        Context targetContext = getContext();
        Intent intent = new Intent(targetContext, WeatherDrawerActivity.class);

        mActivityRule.launchActivity(intent);

        String[] airports = airportList.split("\\s+");
        for (int i = 0; i < airports.length; ++i) {

            scrollToRecyclerPosition(R.id.airport_metar_taf_recycler_view, i);
            ViewInteraction viewInteraction = onView(withRecyclerView(R.id.airport_metar_taf_recycler_view).atPosition(i));

            // Check the row has the airport id field
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_station_id))));
            // Check that the current position has the correct airport id
            viewInteraction.check(matches(hasDescendant(withText(airports[i]))));
            // Check that metar layout exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_weather_include_metar))));
            // Check that metar field exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_station_raw_text))));
        }
    }

    @After
    public void after() {
        Timber.d("After Called");
    }

}
