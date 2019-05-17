package org.soaringforecast.rasp;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.soaringforecast.rasp.utils.StringUtils;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class LatLngForecastTest {

    Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testPointForecastParmHashMap(){
        HashMap<String,String> hashMap =  StringUtils.getHashMapFromStringRes(context, R.string.point_forecast_parm_conversion);
        assertNotNull(hashMap);
        assertEquals("wstar bsratio", hashMap.get("wstar_bsratio"));
        assertEquals("press1000 press1000wspd press1000wdir", hashMap.get("press1000"));
    }
}
