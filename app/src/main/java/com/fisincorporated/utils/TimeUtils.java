package com.fisincorporated.utils;


import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    public static final SimpleDateFormat GMT_SIMPLE_DATE_FORMAT = new SimpleDateFormat
            ("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    public static final SimpleDateFormat LOCAL_DATE_FORMAT = new SimpleDateFormat("MMM dd, h:mm a");

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

}
