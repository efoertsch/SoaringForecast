package com.fisincorporated.aviationweather;

import android.content.Context;
import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.fisincorporated.aviationweather.app.BaseTest;
import com.fisincorporated.aviationweather.drawer.WeatherDrawerActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
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
        Log.d("Setup", "Setup called");
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

        onView(withId(R.id.app_drawer_layout)).perform(swipeRight());
        onView(withId(R.id.app_weather_drawer)).check(matches(isDisplayed()));

    }
}