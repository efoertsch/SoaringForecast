package com.fisincorporated.aviationweather.test;


import com.fisincorporated.aviationweather.satellite.SatelliteImageDownloader;
import com.fisincorporated.aviationweather.satellite.SatelliteImageInfo;
import com.fisincorporated.aviationweather.utils.TimeUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertTrue;

public class SatelliteImageDownloaderTest {

    public SatelliteImageDownloaderTest() {
    }


    @Test
    public void calendarRoundsDownToQuarterHourTest() {
        Calendar rightNow = Calendar.getInstance();

        rightNow.set(Calendar.MINUTE, 0);
        checkRoundedDownTime(rightNow, 0);
        rightNow.set(Calendar.MINUTE, 1);
        checkRoundedDownTime(rightNow, 0);
        rightNow.set(Calendar.MINUTE, 14);
        checkRoundedDownTime(rightNow, 0);

        rightNow.set(Calendar.MINUTE, 15);
        checkRoundedDownTime(rightNow, 15);
        rightNow.set(Calendar.MINUTE, 16);
        checkRoundedDownTime(rightNow, 15);
        rightNow.set(Calendar.MINUTE, 29);
        checkRoundedDownTime(rightNow, 15);

        rightNow.set(Calendar.MINUTE, 30);
        checkRoundedDownTime(rightNow, 30);
        rightNow.set(Calendar.MINUTE, 31);
        checkRoundedDownTime(rightNow, 30);
        rightNow.set(Calendar.MINUTE, 44);
        checkRoundedDownTime(rightNow, 30);

        rightNow.set(Calendar.MINUTE, 45);
        checkRoundedDownTime(rightNow, 45);
        rightNow.set(Calendar.MINUTE, 46);
        checkRoundedDownTime(rightNow, 45);
        rightNow.set(Calendar.MINUTE, 59);
        checkRoundedDownTime(rightNow, 45);

    }

    public void checkRoundedDownTime(Calendar currentTime, int roundedDownMinutes) {
        Calendar calendarRoundedDown = (Calendar) currentTime.clone();
        TimeUtils.setCalendarToQuarterHour(calendarRoundedDown);
        assertTrue("Rounded down minutes not:" + roundedDownMinutes, calendarRoundedDown.get(Calendar.MINUTE) == roundedDownMinutes);

    }

    @Test
    public void checkFormatOfSatelliteDate() {
        String formattedDateString = "20170420_1530";
        Calendar imageDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        imageDate.set(Calendar.YEAR, 2017);
        // Note month of year is zero based so month 3 = April
        imageDate.set(Calendar.MONTH, 3);
        imageDate.set(Calendar.DAY_OF_MONTH, 20);
        imageDate.set(Calendar.HOUR_OF_DAY, 15);
        imageDate.set(Calendar.MINUTE, 36);
        TimeUtils.setCalendarToQuarterHour(imageDate);
        String imageDateString = TimeUtils.formatCalendarToSatelliteImageUtcDate(TimeUtils.setCalendarToQuarterHour(imageDate));
        assertThat("Satellite Image date format incorrect", formattedDateString, equalTo(imageDateString));

    }

    @Test
    public void makeSure15ImageTimes(){
        SatelliteImageInfo satelliteImageInfo = SatelliteImageDownloader.createSatelliteImageInfo(Calendar.getInstance(TimeZone.getTimeZone("UTC")), "alb", "vis");
        assertThat("Must be 15 images", satelliteImageInfo.getSatelliteImageNames().size(),equalTo(15));
    }

    @Test
    public void checkImageSuffix() {
        String imageSuffix = SatelliteImageDownloader.getImageNameSuffix("alb" ,"vis");
        assertThat("Image suffix not formated correctly", imageSuffix,  equalTo("_ALB_vis.jpg"));
    }

}
