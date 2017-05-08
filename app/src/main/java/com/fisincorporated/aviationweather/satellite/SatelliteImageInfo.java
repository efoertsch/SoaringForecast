package com.fisincorporated.aviationweather.satellite;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


/**
 * Hold information on satellite image names/times
 */
public class SatelliteImageInfo {

    private static final String imageTimeFormat = "MM/dd HH:mm";
    private DateFormat utcFormat;
    private DateFormat localFormat;
    private List<String> satelliteImageNames = new ArrayList<>();
    private List<String> satelliteImageUTCTimes = new ArrayList<>();
    private List<String> satelliteImageLocalTimes = new ArrayList<>();

    public SatelliteImageInfo() {
        utcFormat = new SimpleDateFormat(imageTimeFormat);
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        localFormat = new SimpleDateFormat(imageTimeFormat);
        localFormat.setTimeZone(TimeZone.getDefault());
    }

    public void addSatelliteImageInfo(String satelliteImageName, Calendar satelliteImageTime, int index) {
        satelliteImageNames.add(index, satelliteImageName);
        satelliteImageUTCTimes.add(index, utcFormat.format(satelliteImageTime.getTime()));
        satelliteImageLocalTimes.add(index, localFormat.format(satelliteImageTime.getTime()));
    }

    public List<String> getSatelliteImageNames() {
        return satelliteImageNames;
    }

    public List<String> getSatelliteImageUTCTimes() {
        return satelliteImageUTCTimes;
    }

    public List<String> getSatelliteImageLocalTimes() {
        return satelliteImageLocalTimes;
    }


}
