package com.fisincorporated.soaringforecast;

import android.content.Context;
import android.content.Intent;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.fisincorporated.soaringforecast.app.BaseTest;
import com.fisincorporated.soaringforecast.drawer.WeatherDrawerActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isOpen;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * A coding exercise in creating tests prior to implementing the actual code
 * Hopefully there where be no cheating along the way...
 */
@RunWith(AndroidJUnit4.class)
public class WeatherDrawerActivityTest extends BaseTest {

    // Hmmm - Won't even compile without having at least the activity defined.
    @Rule
    public ActivityTestRule<WeatherDrawerActivity> mActivityRule =
            new ActivityTestRule<>(WeatherDrawerActivity.class, true, false);

    @Before
    public void setup() {
        super.setup();
    }

    // Make sure activity can start
    @Test
    public void weatherDrawerActivityExists() {
        Context targetContext = getContext();
        Intent intent = new Intent(targetContext, WeatherDrawerActivity.class);
        mActivityRule.launchActivity(intent);
    }


    @Test
    public void drawerExistsSwipingRight() {
        weatherDrawerActivityExists();
        onView(withId(R.id.app_drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.app_drawer_layout)).check(matches(isOpen()));
    }

    @Test
    public void canGetToAirportListFromNavDrawer() {
        drawerExistsSwipingRight();
        onView(withId(R.id.app_weather_drawer)).perform(NavigationViewActions.navigateTo(R.id.nav_menu_airport_list));
    }

    @Test
    public void canGetToSettingsFromNavDrawer() {
        drawerExistsSwipingRight();
        onView(withId(R.id.app_weather_drawer)).perform(NavigationViewActions.navigateTo(R.id.nav_menu_display_options));


    }
}