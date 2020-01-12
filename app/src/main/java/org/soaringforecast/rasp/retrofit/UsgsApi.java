package org.soaringforecast.rasp.retrofit;

import org.soaringforecast.rasp.turnpoints.json.NationalMap;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UsgsApi {

    /** Get elevation at lat/long
     *
     * @param latitude - Decimal degrees  dd.ddd   south is negative
     * @param longitude - decimal degrees ddd.ddd  west is negative
     * @param units - Feet or Meters
     * @return  NationalMap (contains elevation at lat/long in units amount
     */

    @GET("https://nationalmap.gov/epqs/pqs.php?output=json")
    Single<NationalMap> getElevation(@Query("y") String latitude
            , @Query("x") String longitude
            , @Query("units") String units);

}
