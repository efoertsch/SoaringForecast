package com.fisincorporated.aviationweather.common;

public class Constants {
    // used to create soaring forecast URLs
    //TODO Create ENUM
    public static final String BODY = "body";
    public static final String HEAD = "head";
    public static final String SIDE = "side";
    public static final String FOOT = "foot";

    public enum FORECAST_ACTION {
        FORWARD,
        BACKWARD,
        LOOP
    }

   public enum FORECAST_SOUNDING {
        FORECAST, SOUNDING
    }



}
