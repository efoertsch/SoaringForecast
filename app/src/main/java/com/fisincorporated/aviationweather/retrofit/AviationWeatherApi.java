package com.fisincorporated.aviationweather.retrofit;

import com.fisincorporated.aviationweather.data.metars.MetarResponse;
import com.fisincorporated.aviationweather.data.taf.TafResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AviationWeatherApi {

    int TAF_HOURS_BEFORE_NOW = 7;
    /**
     * Get most recent TAFs issued within last x hours
     *
     * @param icaoIdentifiers whitespace or comma delimited list of airport icao codes
     * @param hoursBeforeNow
     * @return
     */
    @GET("httpparam?dataSource=metars&requestType=retrieve&format=xml&mostRecentForEachStation=constraint")
    Call<MetarResponse> mostRecentMetarForEachAirport(@Query("stationString") String icaoIdentifiers
            , @Query("hoursBeforeNow") int hoursBeforeNow);


    int METAR_HOURS_BEFORE_NOW = 2;
    /**
     * Get most recent Metars issued within last x hours
     *
     * @param icaoIdentifiers whitespace or comma delimited list of airport icao codes
     * @param hoursBeforeNow
     * @return
     */
    @GET("httpparam?dataSource=tafs&requestType=retrieve&format=xml&mostRecentForEachStation=constraint")
    Call<TafResponse> mostRecentTafForEachAirport(@Query("stationString") String icaoIdentifiers
            , @Query("hoursBeforeNow") int hoursBeforeNow);
}
