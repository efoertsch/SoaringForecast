package org.soaringforecast.rasp.retrofit;

import org.soaringforecast.rasp.data.metars.MetarResponse;
import org.soaringforecast.rasp.data.taf.TafResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AviationWeatherGovApi {


    /**
     * Get most recent TAFs issued within last x hours
     * TAFs are issued at least four times a day, every six hours, for major civil airfields: 0000, 0600, 1200 and 1800 UTC
     *
     * @param icaoIdentifiers whitespace or comma delimited list of airport icao codes
     * @param hoursBeforeNow
     * @return
     */
    @GET("httpparam?dataSource=metars&requestType=retrieve&format=xml&mostRecentForEachStation=constraint")
    Call<MetarResponse> getMostRecentMetarForEachAirport(@Query("stationString") String icaoIdentifiers
            , @Query("hoursBeforeNow") int hoursBeforeNow);


    /**
     * Get most recent Metars issued within last x hours
     * The METAR only gives you a small snapshot in time. They are only good for an hour. They are usually refreshed around 55 past the hour.
     *
     * @param icaoIdentifiers whitespace or comma delimited list of airport icao codes
     * @param hoursBeforeNow
     * @return
     */
    @GET("httpparam?dataSource=tafs&requestType=retrieve&format=xml&mostRecentForEachStation=constraint")
    Call<TafResponse> getMostRecentTafForEachAirport(@Query("stationString") String icaoIdentifiers
            , @Query("hoursBeforeNow") int hoursBeforeNow);
}
