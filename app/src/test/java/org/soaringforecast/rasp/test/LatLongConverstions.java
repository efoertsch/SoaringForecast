package org.soaringforecast.rasp.test;

import org.junit.Test;
import org.soaringforecast.rasp.repository.Turnpoint;

import static org.junit.Assert.assertEquals;

public class LatLongConverstions {

    @Test
    public void testLatitudeConversion() throws Exception {
        String lat = "4149.956N";
        String lng = "07352.558W";
        assertEquals(lat, Turnpoint.getLatitudeInCupFormat(Turnpoint.convertToLat(lat)));
        assertEquals(lng, Turnpoint.getLongitudeInCupFormat(Turnpoint.convertToLong(lng)));

        lat = "4149.956S";
        lng = "07352.558E";
        assertEquals(lat, Turnpoint.getLatitudeInCupFormat(Turnpoint.convertToLat(lat)));
        assertEquals(lng, Turnpoint.getLongitudeInCupFormat(Turnpoint.convertToLong(lng)));
    }

}
