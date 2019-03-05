package org.soaringforecast.rasp.satellite.data;

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
    private static DateFormat utcFormat;
    private static DateFormat localFormat;
    private List<String> satelliteImageNames = new ArrayList<>();
    private List<String> satelliteImageUTCTimes = new ArrayList<>();
    private List<String> satelliteImageLocalTimes = new ArrayList<>();
    private List<Calendar> satelliteImageCalendars = new ArrayList<>();

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
        satelliteImageCalendars.add(index, satelliteImageTime);
    }

    public List<String> getSatelliteImageNames() {
        return satelliteImageNames;
    }

    public String getSatelliteImageName(int index) {
        if (index < satelliteImageNames.size()) {
            return satelliteImageNames.get(index);
        }
        return null;
    }

    public List<String> getSatelliteImageUTCTimes() {
        return satelliteImageUTCTimes;
    }

    public String getSatelliteImageUTCTime(int index) {
        if (index < satelliteImageUTCTimes.size()) {
            return satelliteImageUTCTimes.get(index);
        }
        return null;
    }

    public List<String> getSatelliteImageLocalTimes() {
        return satelliteImageLocalTimes;
    }

    public String getSatelliteImageLocalTime(int index) {
        if (index < satelliteImageLocalTimes.size()) {
            return satelliteImageLocalTimes.get(index);
        }
        return null;
    }

    public List<Calendar> getSatelliteImageCalendars() {
        return satelliteImageCalendars;
    }

    public Calendar getSatelliteImageCalendar(int index) {
        if (index < satelliteImageCalendars.size()) {
            return satelliteImageCalendars.get(index);
        }
        return null;
    }
}
