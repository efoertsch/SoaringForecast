package org.soaringforecast.rasp.test;

import org.junit.Test;
import org.soaringforecast.rasp.utils.CSVUtils;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class CSVParseTest {

    @Test
    public void testCommasWithinDoubleQuotes() {
        String testTurnpoint = "\"Albert Farms\",\"MA88\",US,4223.400N,07255.850W,1424ft,5,180,2800ft,,\"Private, MA88, N/S 28A, RW width: 50\"";
        List turnpoint = CSVUtils.parseLine(testTurnpoint);
        assertTrue(turnpoint.get(0).equals("Albert Farms"));
        assertTrue(turnpoint.get(1).equals("MA88"));
        assertTrue(turnpoint.get(2).equals("US"));
        assertTrue(turnpoint.get(3).equals("4223.400N"));
        assertTrue(turnpoint.get(4).equals("07255.850W"));
        assertTrue(turnpoint.get(5).equals("1424ft"));
        assertTrue(turnpoint.get(6).equals("5"));
        assertTrue(turnpoint.get(7).equals("180"));
        assertTrue(turnpoint.get(8).equals("2800ft"));
        assertTrue(turnpoint.get(9).equals(""));
        assertTrue(turnpoint.get(10).equals("Private, MA88, N/S 28A, RW width: 50"));
    }


}
