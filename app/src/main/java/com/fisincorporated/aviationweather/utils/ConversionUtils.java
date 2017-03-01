package com.fisincorporated.aviationweather.utils;


import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ConversionUtils {

    public static final SimpleDateFormat GMT_SIMPLE_DATE_FORMAT = new SimpleDateFormat
            ("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    public static final SimpleDateFormat LOCAL_DATE_FORMAT = new SimpleDateFormat("MMM dd, h:mm a");

    public static float convertCentigradeToFahrenheit(Float tempC) {

        return tempC == null ? 0 : (tempC * 1.8f) + 32f;
    }

    public static float convertFahrenheitToCentigrade(Float tempF) {

        return tempF == null ? 0 : (tempF - 32) / 1.8f;
    }

    public static int convertMetersToFeet(Float meters) {
        return (meters == null) ? 0 : Math.round(meters * 3.28084f);
    }


    public static String convertGMTToLocalTime(@NonNull String gmtTime) {

        try {
            GMT_SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            Date date = GMT_SIMPLE_DATE_FORMAT.parse(gmtTime);
            return LOCAL_DATE_FORMAT.format(date);
        } catch (ParseException e) {
            e.printStackTrace();

        }

        return "Invalid GMT time: " + gmtTime;
    }

    public static String getAirportListForMetars(String airportList) {
        StringBuilder sb = new StringBuilder();
        if (airportList != null) {
            String[] airportArray  = airportList.trim().split(" ");
            for (int i = 0; i < airportArray.length; ++i){
                sb.append(airportArray[i] + (i < airportArray.length - 1 ? ", " : ""));
            }
        }
        return sb.toString();
    }

}
