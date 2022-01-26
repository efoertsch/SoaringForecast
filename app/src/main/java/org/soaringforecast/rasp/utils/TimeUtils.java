package org.soaringforecast.rasp.utils;


import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import timber.log.Timber;

public class TimeUtils {

    // Satellite image date format  20180326/16/20180326_1600
    private static final SimpleDateFormat satelliteImageDateFormat = new SimpleDateFormat("yyyyMMdd/HH/yyyyMMdd_HHmm", Locale.US);
    private static final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
    private static final SimpleDateFormat geosCacheSimpleDateFormat = new SimpleDateFormat("yyMMddHHmm", Locale.US);
    public static final SimpleDateFormat wxbriefTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

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

    private static Calendar roundDownCalenderToXMinInterval(Calendar calendar, int interval) {
        int minutesPastHour = calendar.get(Calendar.MINUTE);
        int i = 0;
        while (i <= 59 ) {
            if (minutesPastHour < i + interval) {
                calendar.set(Calendar.MINUTE, i);
                break;
            }
            i = i + interval;
        }
        return calendar;
    }



    @SuppressLint("DefaultLocale")
    public static String formatCalendarToSatelliteImageUtcDate(Calendar calendar) {
        satelliteImageDateFormat.setTimeZone(utcTimeZone);
        return satelliteImageDateFormat.format(calendar.getTime());
        // Construct time part of url string like 20180326/16/20180326_1600
//        return String.format("%d%02d%02d/%02d/%d%02d%02d_%02d%2d"
//                , calendar.get(Calendar.YEAR)
//                , calendar.get(Calendar.MONTH) + 1
//                , calendar.get(Calendar.DAY_OF_MONTH)
//                , calendar.get(Calendar.HOUR_OF_DAY)
//                , calendar.get(Calendar.YEAR)
//                , calendar.get(Calendar.MONTH) + 1
//                , calendar.get(Calendar.DAY_OF_MONTH)
//                , calendar.get(Calendar.HOUR_OF_DAY)
//                , calendar.get(Calendar.MINUTE));
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

    /**
     * Add (+minutes) or substract (-minutes) from calender
     * @param calendar
     * @param minutes  Add minutes if positive or substract if negative
     * @return   new calender +/- minutes
     */
    public static Calendar addXMinutesToCalendar(Calendar calendar, int minutes){
        Calendar calendarPlusXMinutes = (Calendar) calendar.clone();
        calendarPlusXMinutes.add(Calendar.MINUTE, minutes);
        return calendarPlusXMinutes;
    }

    public static Calendar getUtcRightNow() {
        return Calendar.getInstance(utcTimeZone);
    }

    public static String getGeosLoopCacheKey(){
        return geosCacheSimpleDateFormat.format(TimeUtils.setCalendarToQuarterHour(TimeUtils.getUtcRightNow()).getTime());
    }

    public static String getGeosLoopCacheKey(int interval){
        return geosCacheSimpleDateFormat.format(TimeUtils.roundDownCalenderToXMinInterval(TimeUtils.getUtcRightNow(), interval).getTime());
    }

    /**
     * Convert a local date/time to a zulu date time.
     * Note that localTime actually gets converted to Zulu time but need to add time zone offset
     * between localTime zone and Zulu (GMT) timezon
     *
     * @param localTime
     * @return
     */
    public static String convertLocalTimeToZulu(String localTime) {
        Timber.d("Local time: %1$s", localTime);
        long zuluTimeMillis = convertDateToMillis(localTime);
        // add in current time difference
        int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
        long realZuluTIme = zuluTimeMillis + ((offset > 0) ? offset : -1 * offset);
        String zuluTime = wxbriefTimeFormatter.format(realZuluTIme);
        Timber.d("Zulu time: %1$s", zuluTime);
        return zuluTime;
    }

    /**
     * Convert formatted date to milliseconds
     * The formatted date must be in the form of
     * "yyyy-MM-ddTHH:mm:ss.S"
     * e.g. 2020-08-15T23:00:00.0
     */

    public static long convertDateToMillis(String date) {
        try {
            Date mDate = wxbriefTimeFormatter.parse(date);
            long timeInMilliseconds = mDate.getTime();
            System.out.println("Date in millis : " + timeInMilliseconds);
            return timeInMilliseconds;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    public static String getLocalTimeZoneAbbrev() {
        String zoneAbbrev;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ZoneId zone = ZoneId.systemDefault();
            DateTimeFormatter zoneAbbreviationFormatter
                    = DateTimeFormatter.ofPattern("zzz", Locale.ENGLISH);
            zoneAbbrev = ZonedDateTime.now(zone).format(zoneAbbreviationFormatter);
            Timber.d("Current abbreviation for either standard or summer time: %1$s "
                    , zoneAbbrev);

        } else {
            zoneAbbrev = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
        }
        return zoneAbbrev;
    }



}
