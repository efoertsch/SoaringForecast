package org.soaringforecast.rasp.retrofit;



import org.soaringforecast.rasp.soaring.json.ForecastModels;
import org.soaringforecast.rasp.soaring.json.Regions;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Calls to www.soargbsc.com/rasp
 */
public interface SoaringForecastApi {

    /**
     * Get initial JSON from server that contains available forecast regions, soundings available for the region
     * and list of dates for which forecasts are available
     * @param currentJson - something like "current.json"
     * @return
     */
    @GET("/rasp/{currentJson}")
    Single<Regions> getForecastDates(@Path("currentJson") String currentJson);


    /**
     * For given region and date find the
     *  - forecast models (gfs, nam,...) that are available
     *  - lat/long corners for that model forecast
     *  - and times that the model forecast is available
     * @param region - e.g. "NewEngland
     * @param date - e.g. 2018-03-30"
     * @return
     */

    @GET("/rasp/{region}/{date}/status.json")
    Single<ForecastModels> getForecastModels(@Path("region") String region, @Path("date") String date);


    /**
     * For the given lat/long get a set of point forecasts for the given region/date/model/time
     * @param region - e.g. NewEngland
     * @param date - e.g. 2018-03-30
     * @param model - e.g. gfs
     * @param time - e.g. 1100
     * @param lat - latitude
     * @param lon - longitude
     * @param forecastType - e.g. "wstar bsratio zsfclcldif zsfclcl zblcldif zblcl ... "
     * @return - response body that contains text forecast(s)
     */
    @POST("cgi/get_rasp_blipspot.cgi")
    @FormUrlEncoded
    Single<Response<ResponseBody>> getLatLongPointForecast(
            @Field(value = "region", encoded = true) String region,
            @Field(value = "date", encoded = true) String date,
            @Field(value = "model", encoded = true) String model,
            @Field(value = "time", encoded = true) String time,
            @Field(value = "lat", encoded = true) String lat,
            @Field(value = "lon", encoded = true) String lon,
            @Field(value = "param", encoded = true) String forecastType
    );

}
