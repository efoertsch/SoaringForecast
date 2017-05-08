package com.fisincorporated.aviationweather.utils;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class TimeUtils {

    // Satellite image date format 20170319_1645;
    public static final SimpleDateFormat satelliteImageDateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
    public static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

    /**
     * Round down calendar to quarter hour
     * @param calendar
     * @return
     */
    public static Calendar setCalendarToQuarterHour(Calendar calendar) {
        int minutesPastHour = calendar.get(Calendar.MINUTE);
        if (minutesPastHour < 15) {
            calendar.set(Calendar.MINUTE, 0);
        } else if (minutesPastHour < 30) {
            calendar.set(Calendar.MINUTE, 15);
        } else if (minutesPastHour < 45) {
            calendar.set(Calendar.MINUTE, 30);
        } else if (minutesPastHour <= 59) {
            calendar.set(Calendar.MINUTE, 45);
        }
        return calendar;
    }

    public static String formatCalendarToSatelliteImageUtcDate(Calendar calendar) {
        satelliteImageDateFormat.setTimeZone(utcTimeZone);
        return satelliteImageDateFormat.format(calendar.getTime());
    }

    /**
     * Substract 15 minutes
     * @param calendar
     * @return
     */
    public static Calendar subtract15MinutesFromCalendar(Calendar calendar){
        Calendar calendarMinus15Minutes = (Calendar) calendar.clone();
          calendarMinus15Minutes .add(Calendar.MINUTE, -15);
        return calendarMinus15Minutes;
    }

    public static Calendar getUtcRightNow() {
        return Calendar.getInstance(utcTimeZone);
    }

}
