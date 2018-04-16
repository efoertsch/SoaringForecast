package com.fisincorporated.aviationweather.retrofit;



import com.fisincorporated.aviationweather.soaring.json.RegionForecastDates;
import com.fisincorporated.aviationweather.soaring.json.TypeLocationAndTimes;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Calls to www.soargbsc.com/rasp
 */
public interface SoaringForecastApi {

    String BASE_URL = "http:://www.soargbsc.com/rasp/";

    // Construct URL as BASE_URL + "current.json?123456"
    // Numbers at end are current time in millisec (used to always get updated json and
    // not server cached version
    @GET()
    Single<RegionForecastDates> getForecastDates(@Url String url);


    // Construct URL as BASE_URL + "NewEngland/2018-03-30/status.json"
    // Numbers at end are current time in millisec (used to always get updated json and
    // not server cached version
    @GET
    Single<TypeLocationAndTimes> getTypeLocationAndTimes(@Url String url);


}
