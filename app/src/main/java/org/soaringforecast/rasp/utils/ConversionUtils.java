package org.soaringforecast.rasp.utils;


import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ConversionUtils {

    private static final SimpleDateFormat GMT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat LOCAL_DATE_FORMAT = new SimpleDateFormat("MMM dd, h:mm a");

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat SHORT_DATE_TIME_FORMAT = new SimpleDateFormat("MM/dd h:mm a");

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat SHORT_TIME_FORMAT = new SimpleDateFormat("h:mm a");


    public static float convertCentigradeToFahrenheit(Float tempC) {

        return tempC == null ? 0 : (tempC * 1.8f) + 32f;
    }

    public static float convertFahrenheitToCentigrade(Float tempF) {

        return tempF == null ? 0 : (tempF - 32) / 1.8f;
    }

    public static int convertMetersToFeet(Float meters) {
        return (meters == null) ? 0 : Math.round(meters * 3.28084f);
    }

    public static String convertGMTToLocalTimeString(@NonNull String gmtTime) {
        try {
            Date gmtDate = getGmtDate(gmtTime);
            return LOCAL_DATE_FORMAT.format(gmtDate);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return "Invalid GMT time: " + gmtTime;
    }

    public static String convertGMTToLocalTime(@NonNull String gmtTime) {
        try {
            Date gmtDate = getGmtDate(gmtTime);
            return LOCAL_DATE_FORMAT.format(gmtDate);
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return "Invalid GMT time: " + gmtTime;
    }

    public static Date getGmtDate(@NonNull String gmtTime) throws ParseException {
        GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return GMT_DATE_FORMAT.parse(gmtTime);
    }

    public static String formatGmtDateAsString(Date gmtDate) {
        GMT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return GMT_DATE_FORMAT.format(gmtDate);

    }

    // Formats to local date/time
    public static String formatToShortDateTime(@NonNull String gmtTime) {
        if (gmtTime != null) {
            Date gmtDate = null;

            try {
                gmtDate = getGmtDate(gmtTime);
                return SHORT_DATE_TIME_FORMAT.format(gmtDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return "Invalid GMT time!";
    }

    // Formats to local time
    public static String formatToShortTime(String gmtTime) {
        if (gmtTime != null) {
            Date gmtDate = null;
            try {
                gmtDate = getGmtDate(gmtTime);
                return SHORT_TIME_FORMAT.format(gmtDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return "Invalid GMT time!";
    }

    public static String howLongAgo(String gmtTime) {
        try {
            Date gmtDate = getGmtDate(gmtTime);
            Map<TimeUnit, Long> timeDifference = computeDifferenceInDates(gmtDate, new Date());
            return String.format(Locale.getDefault(),"%1$d Days %2$d Hours %3$d Minutes Old", timeDifference.get(TimeUnit.DAYS)
                    , timeDifference.get(TimeUnit.HOURS), timeDifference.get(TimeUnit.MINUTES));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Invalid GMT Time!";
    }

    // http://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java/35082080#35082080
    public static Map<TimeUnit, Long> computeDifferenceInDates(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        List<TimeUnit> units = new ArrayList<>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);
        Map<TimeUnit, Long> result = new LinkedHashMap<>();
        long milliesRest = diffInMillies;
        for (TimeUnit unit : units) {
            long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;
            result.put(unit, diff);
        }
        return result;
    }

}
