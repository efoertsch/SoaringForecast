package org.soaringforecast.rasp.test;

import org.soaringforecast.rasp.utils.TimeUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeUtilsTest {

    @Test
    public void roundDownTimeStringToIntervalTest() {
        assertEquals("1810301105", TimeUtils.getGeosLoopCacheKey( 5));
    }
}
