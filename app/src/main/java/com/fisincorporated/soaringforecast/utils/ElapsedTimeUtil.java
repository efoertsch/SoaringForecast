package com.fisincorporated.soaringforecast.utils;

public class ElapsedTimeUtil {

    private static final String TAG = ElapsedTimeUtil.class.getSimpleName();
    private static long startingMillisTime;
    private static long totalElapsedTime = 0;
    private static long timeSinceLastCall = 0;
    private static long currentMillisTime;
    private static long timeOfLastCall= 0;

    public static void init(){
        startingMillisTime = System.currentTimeMillis();
        currentMillisTime = 0;
        totalElapsedTime = 0;
        timeSinceLastCall = 0;
        timeOfLastCall = startingMillisTime;
        showElapsedTime(TAG,"Initialization");
    }
    public static void showElapsedTime(String tag, String description){
        currentMillisTime = System.currentTimeMillis();
        totalElapsedTime = currentMillisTime - startingMillisTime;
        timeSinceLastCall = currentMillisTime - timeOfLastCall;
        System.out.println(tag + " " + description + "  totalElapsedTime:" + totalElapsedTime + " timeSinceLastCall: " + timeSinceLastCall);
        timeOfLastCall = currentMillisTime;

    }
}
