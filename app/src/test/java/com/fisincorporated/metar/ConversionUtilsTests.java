package com.fisincorporated.metar;

import com.fisincorporated.aviationweather.utils.ConversionUtils;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ConversionUtilsTests {

    @Test
    public void conversionFromGmtToLocalTimeIsCorrect() throws Exception {
        //Hmmm - How to test when we go between EST and DST?
        assertEquals("Feb 27, 3:54 PM", ConversionUtils.convertGMTToLocalTimeString("2017-02-27T20:54:00Z"));
    }

    @Test
    public void checkHowLongAgoIsCorrect() throws Exception {
        String gmtDateString = "2017-03-07T11:35:00Z";
        Date date = ConversionUtils.getGmtDate(gmtDateString);
        assertEquals(ConversionUtils.formatGmtDateAsString(date), gmtDateString);
    }

    @Test
    public void checkComputeDifferenceInDatesTest() throws Exception {
        String gmtDateString = "2017-03-07T11:35:00Z";
        Date date = ConversionUtils.getGmtDate(gmtDateString);
        Long dateMillis = date.getTime();
        // Subtract 1 day, 1 hour, 1 min
        dateMillis = dateMillis - (24 * 60 * 60 * 1000) - (60 * 60 * 1000) - (60 * 1000);
        Date newDate = new Date(dateMillis);
        assertEquals(ConversionUtils.formatGmtDateAsString(newDate), "2017-03-06T10:34:00Z");
    }

    @Test
    public void formatToShortDateTimeTest() {
        //Hmmm - How to test when we go between EST and DST?
        String gmtDateString = "2017-03-07T11:35:00Z";
        assertEquals(ConversionUtils.formatToShortDateTime("2017-03-07T11:35:00Z"),"03/07 6:35");
    }

    @Test
    public void formatToShortTimeTest() {
        //Hmmm - How to test when we go between EST and DST?
        String gmtDateString = "2017-03-07T11:35:00Z";
        assertEquals(ConversionUtils.formatToShortTime("2017-03-07T11:35:00Z"),"6:35 AM");
    }

}
