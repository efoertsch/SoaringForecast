package org.soaringforecast.rasp.repository;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class TurnpointTest {

    String turnpointString = "\"Sterling\",\"3B3\",US,4225.500N,07147.470W,459ft,5,160,3086ft,122.900,\"Home Field, Finish Point, Turn Point, 3B3, RW width: 40, CTAF: 122.9, Fuel: 100LL\"";

    @Test
    public void convertFromStringToTurnpointTest() {
        Turnpoint turnpoint = Turnpoint.createTurnpointFromCSVDetail(turnpointString);
        assertNotNull(turnpoint);
        assertEquals("Incorrect title", "Sterling",turnpoint.getTitle());
        assertEquals("Incorrect code", "3B3",turnpoint.getCode());
        assertEquals("Incorrect country", "US", turnpoint.getCountry());
        assertEquals("Incorrect altitude", "459ft", turnpoint.getElevation());
        assertEquals("Incorrect style", "5", turnpoint.getStyle());
        assertEquals("Incorrect direction", "160", turnpoint.getDirection());
        assertEquals("Incorrect length","3086ft", turnpoint.getLength());
        assertEquals("Incorrect frequency", "122.900", turnpoint.getFrequency());
        assertEquals("Incorrect description", "Home Field, Finish Point, Turn Point, 3B3, RW width: 40, CTAF: 122.9, Fuel: 100LL", turnpoint.getDescription());
    }

    @Test
    public void northLatitudeConversionTest() throws Exception {
        float latitude = Turnpoint.convertToLat("4225.500N");
        String convertedLatString = String.format("%.5f", latitude);
        assertEquals(" Latitude not converted correctly assertEquals(\"Incorrect title\", \"Sterling\",turnpoint.getTitle());", "42.42500", convertedLatString );
    }

    @Test
    public void southLatitudeConversionTest() throws Exception {
        float latitude = Turnpoint.convertToLat("4225.500S");
        String convertedLatString = String.format("%.5f", latitude);
        assertEquals(" Latitude not converted correctly", "-42.42500", convertedLatString );
    }


    @Test
    public void westLongitudeConversionTest() throws Exception {
        float longitude = Turnpoint.convertToLong("07147.470W");
        String convertedLongString = String.format("%.6f", longitude);
        assertEquals(" longitude not converted correctly", "-71.791168", convertedLongString );
    }

    @Test
    public void eastLongitudeConversionTest() throws Exception {
        float longitude = Turnpoint.convertToLong("07147.470E");
        String convertedLongString = String.format("%.6f", longitude);
        assertEquals(" longitude not converted correctly", "71.791168", convertedLongString );
    }
}
