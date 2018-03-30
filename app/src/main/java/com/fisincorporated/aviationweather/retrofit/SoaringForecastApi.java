package com.fisincorporated.aviationweather.retrofit;



import com.fisincorporated.aviationweather.soaring.json.ForecastDates;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Calls to www.soargbsc.com/rasp
 */
public interface SoaringForecastApi {

    String BASE_URL = "http:://www.soargbsc.com/rasp/";

    // Construct URL as BASE_URL + "current.json?123456"
    // Numbers are end are current time in millisec
    @GET
    Call<ForecastDates> getForecastDates(@Url String url);


}
