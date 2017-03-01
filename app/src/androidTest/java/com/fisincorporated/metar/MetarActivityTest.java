package com.fisincorporated.metar;


import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.fisincorporated.aviationweather.airportweather.AirportWeatherActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MetarActivityTest {
    

        @Rule
        public ActivityTestRule<AirportWeatherActivity> mActivityRule =
                new ActivityTestRule<>(AirportWeatherActivity.class, true, false);

        @Test
        public void someTest() {
            Context targetContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext();
            Intent intent = new Intent(targetContext, AirportWeatherActivity.class);
            intent.putExtra(AirportWeatherActivity.METAR_LIST, "KORH, KBOS");

            mActivityRule.launchActivity(intent);
 
        /* Your activity is initialized and ready to go. */
        }
}
