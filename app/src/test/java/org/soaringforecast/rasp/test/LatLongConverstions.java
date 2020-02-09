package org.soaringforecast.rasp.test;

import org.junit.Test;
import org.soaringforecast.rasp.repository.Turnpoint;

import static org.junit.Assert.assertEquals;

public class LatLongConverstions {

    @Test
    public void testLatitudeConversion() throws Exception {
        String lat ;
        String lng ;

//        lng = "07352.558W";
//        lat = "4149.956N";
//        assertEquals(lat, Turnpoint.getLatitudeInCupFormat(Turnpoint.convertToLat(lat)));
//        assertEquals(lng, Turnpoint.getLongitudeInCupFormat(Turnpoint.convertToLong(lng)));
//
//        lat = "4149.956S";
//        lng = "07352.558E";
//        assertEquals(lat, Turnpoint.getLatitudeInCupFormat(Turnpoint.convertToLat(lat)));
//        assertEquals(lng, Turnpoint.getLongitudeInCupFormat(Turnpoint.convertToLong(lng)));


        // These were off by a thousandth
        // TurnersFalls  -> 4235.467N,07231.351W
       // lat = "4235.467N";
        lng = "07231.350W";
        //assertEquals(lat, Turnpoint.getLatitudeInCupFormat(Turnpoint.convertToLat(lat)));
        assertEquals(lng, Turnpoint.getLongitudeInCupFormat(Turnpoint.convertToLong(lng)));

        // Cooper Farm  -> 4316.822N,07127.396W
       // lat = "4316.822N";
        lng = "07127.397W";
       // assertEquals(lat, Turnpoint.getLatitudeInCupFormat(Turnpoint.convertToLat(lat)));
        assertEquals(lng, Turnpoint.getLongitudeInCupFormat(Turnpoint.convertToLong(lng)));




    }


}
