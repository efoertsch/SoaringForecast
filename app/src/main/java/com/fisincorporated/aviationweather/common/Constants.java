package com.fisincorporated.aviationweather.common;

public class Constants {
    // used to create soaring forecast URLs
    //TODO Create ENUM
    public static final String BODY = "body";
    public static final String HEAD = "head";
    public static final String SIDE = "side";
    public static final String FOOT = "foot";

    public static final String SELECTED_TASK = "SELECTED_TASK";

    public enum FORECAST_ACTION {
        FORWARD,
        BACKWARD,
        LOOP
    }

   public enum FORECAST_SOUNDING {
        FORECAST, SOUNDING
    }

    public static final String TURNPOINT_FILE_NAME = "TURNPOINT_FILE_NAME ";



}
