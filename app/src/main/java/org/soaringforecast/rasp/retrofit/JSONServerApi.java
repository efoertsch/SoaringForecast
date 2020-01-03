package org.soaringforecast.rasp.retrofit;

import org.soaringforecast.rasp.soaring.json.SUARegionFiles;
import org.soaringforecast.rasp.turnpoints.json.TurnpointRegions;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface JSONServerApi {

    // Get list of turnpoint files
    @GET("/soaringforecast/turnpoint_regions.json")
    Single<TurnpointRegions> getTurnpointRegions();


    // Get list of SUA files
    @GET("/soaringforecast/sua_regions.json")
    Single<SUARegionFiles> getSUARegions();

    @Streaming
    @GET("/soaringforecast/{suaFilename}")
    Single<Response<ResponseBody>> downloadSuaFile(@Path("suaFilename") String suaFilename);


}
