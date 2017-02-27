package com.fisincorporated.utils;

/**
 * Created by ericfoertsch on 2/27/17.
 */

public class TempUtils {

    public static float convertCentigradeToFahrenheit(float tempC){

        return (tempC * 1.8f) + 32f;
    }

    public static float convertFahrenheitToCentigrade(float tempF){
        return (tempF - 32) / 1.8f;
    }
}
